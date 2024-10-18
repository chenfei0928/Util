package io.github.chenfei0928.android

import com.android.build.gradle.AppExtension
import io.github.chenfei0928.Env
import io.github.chenfei0928.util.buildOutputsDir
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.child
import io.github.chenfei0928.util.forEachAssembleTasks
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.util.Locale

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2023-12-07 16:43
 */
fun Project.applySystemApp() {
    val appExtension = buildSrcAndroid<AppExtension>()
    val keyFile = File(rootProject.rootDir, "gradle/platform.pk8")
    val certFile = File(rootProject.rootDir, "gradle/platform.x509.pem")

    forEachAssembleTasks { assembleTask, taskInfo ->
        val upCaseName = taskInfo.dimensionedFlavorBuildTypeName.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
        tasks.register("buildAndInstall${upCaseName}Apk") {
            dependsOn(assembleTask)
            doLast {
                val apkFileRelativePath = taskInfo.run {
                    if (dimensionedFlavorName.isEmpty()) {
                        // 全apk打包或无productFlavor打包
                        // debug/app-debug.apk
                        "${buildType}/app-${buildType}.apk"
                    } else {
                        // 某个productFlavor打包
                        // flavor/debug/app-flavor-debug.apk
                        "${dimensionedFlavorName}/${buildType}/app-${dimensionedFlavorName}-${buildType}.apk"
                    }
                }
                Env.logger.lifecycle("outputs ${assembleTask.outputs.files.elements.get()}")
                arrayOf(
                    buildOutputsDir.child { "apk" / apkFileRelativePath },
                    layout.buildDirectory.child { "intermediates" / "apk" / apkFileRelativePath },
                ).filter {
                    it.exists() && it.lastModified() > Env.launchTimestamp.time
                }.forEach {
                    signAndInstallApk(appExtension, it, keyFile, certFile)
                }
            }
        }
    }
}

private fun signAndInstallApk(
    appExtension: AppExtension,
    apkFile: File,
    keyFile: File,
    certFile: File,
) {
    Env.logger.lifecycle(
        "resign $apkFile ${DateFormat.getInstance().format(apkFile.lastModified())}"
    )
    val apksigner: File = appExtension.run {
        val buildToolsDir = File(File(sdkDirectory, "build-tools"), buildToolsVersion)
        val isWindows = System.getProperty("os.name").startsWith("Windows")
        if (isWindows) File(buildToolsDir, "apksigner.bat")
        else File(buildToolsDir, "apksigner")
    }
    // 重签名
    val apksignerCode = ProcessBuilder().command(
        apksigner.absolutePath,
        "sign",
        "--key",
        keyFile.absolutePath,
        "--cert",
        certFile.absolutePath,
        apkFile.absolutePath,
    ).apply {
        redirectErrorStream(true)
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
    }.start().waitFor()
    Env.logger.lifecycle("resign $apkFile result $apksignerCode")
    try {
        ProcessBuilder().command(
            appExtension.adbExecutable.absolutePath, "--version"
        ).start().waitFor()
        installApk(appExtension, apkFile)
    } catch (e: IOException) {
        Env.logger.error("adb 执行失败，跳过安装", e)
    }
}

private fun installApk(appExtension: AppExtension, apkFile: File) {
    // 卸载app
    ProcessBuilder().command(
        appExtension.adbExecutable.absolutePath,
        "uninstall",
        appExtension.defaultConfig.applicationId,
    ).apply {
        redirectErrorStream(true)
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
    }.start().waitFor()
    // 安装签名后的app
    val installCode = ProcessBuilder().command(
        appExtension.adbExecutable.absolutePath,
        "install", "-r", "-d", "-t",
        apkFile.absolutePath,
    ).apply {
        redirectErrorStream(true)
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
    }.start().waitFor()
    Env.logger.lifecycle("adb install $apkFile result $installCode")
    // 启动activity
//                val amStartCode = ProcessBuilder().command(
//                    appExtension.adbExecutable.absolutePath, "shell", "am", "start",
//                    buildSrcAndroid<AppExtension>().defaultConfig.applicationId + "/.ui.MainActivity"
//                ).apply {
//                    redirectErrorStream(true)
//                    redirectOutput(ProcessBuilder.Redirect.INHERIT)
//                }.start().waitFor()
//                Env.logger.lifecycle("adb shell am start ${buildSrcAndroid<AppExtension>().defaultConfig.applicationId} result $amStartCode")
}

private fun installApkToPrivilegeApp(
    appExtension: AppExtension, apkFile: File, targetFilePath: String
): Unit = appExtension.run {
    adbCommend("root")
    adbCommend("remount")
    adbCommend("shell", "mkdir", targetFilePath.substringBeforeLast('/'))
    adbCommend("push", apkFile.absolutePath, targetFilePath)
    adbCommend("shell", "sync")
    adbCommend("reboot")
}

private fun command(
    vararg args: String?
): Int = ProcessBuilder().command(*args).apply {
    redirectErrorStream(true)
    redirectOutput(ProcessBuilder.Redirect.INHERIT)
}.start().waitFor()

private fun AppExtension.adbCommend(
    vararg args: String?
): Int = command(adbExecutable.absolutePath, *args).also {
    if (args.firstOrNull() == "shell") {
        Env.logger.debug("\"adb shell ${args[1]}\" return $it")
    } else {
        Env.logger.debug("\"adb ${args.firstOrNull() ?: ""}\" return $it")
    }
}
