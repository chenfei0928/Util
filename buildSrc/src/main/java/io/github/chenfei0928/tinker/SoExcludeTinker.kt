package io.github.chenfei0928.tinker

import com.android.build.api.dsl.ApkSigningConfig
import com.android.build.gradle.AppExtension
import com.google.gradle.osdetector.OsDetectorPlugin
import com.tencent.tinker.build.util.TypedValue
import io.github.chenfei0928.Deps
import io.github.chenfei0928.Env
import io.github.chenfei0928.util.buildOutputsDir
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.checkApp
import io.github.chenfei0928.util.child
import io.github.chenfei0928.util.forEachAssembleTasks
import io.github.chenfei0928.util.implementation
import io.github.chenfei0928.util.replaceFirstCharToUppercase
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import java.io.File
import java.text.DateFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * @author chenf()
 * @date 2025-12-18 14:20
 */
fun Project.applyAppSoExclude() {
    checkApp("applyAppSoExclude")
    apply<OsDetectorPlugin>()

    // 生成 tinkerId 到 manifestPlaceholders
    val putTinkerManifestPlaceholders = putTinkerManifestPlaceholders()

    // 添加 tinkerSdk 依赖
    dependencies {
        implementation(Deps.framework.tinker)
    }

    val appExtension = buildSrcAndroid<AppExtension>()
    val soExcludeList by lazy {
        File(project.projectDir, "so_exclude.txt").readLines()
    }

    afterEvaluate {
        val (tinkerPatchExtensionTriples, _) = createEveryVariantTinkerPatchExtension()

        forEachAssembleTasks { assembleTask, taskInfo ->
            // 对当前的flavor+buildType的输出文件加渠道号
            val variantTinkerPatchExtension = (tinkerPatchExtensionTriples
                .find { it.apkVariantInfo.name == taskInfo.targetFlavorBuildTypeVariantName }
                ?: throw IllegalArgumentException("${taskInfo.targetFlavorBuildTypeVariantName} output file not found: $taskInfo in $tinkerPatchExtensionTriples"))

            val upCaseName = taskInfo.dimensionedFlavorBuildTypeName.replaceFirstCharToUppercase()
            tasks.register("soExcludeTinker${upCaseName}Apk") {
                dependsOn(assembleTask)
                doLast {
                    val newApk = variantTinkerPatchExtension.variantOutput!!.outputFile
                    val baseApk = reZipApkWithFilterEntry(newApk, soExcludeList, appExtension)
                    val signingConfig = appExtension.buildTypes
                        .getByName(taskInfo.buildType).signingConfig!!
                    signApk(appExtension, baseApk, signingConfig)
                    variantTinkerPatchExtension.tinkerPatchExtension.oldApk = baseApk.absolutePath

                    val tinkerId = putTinkerManifestPlaceholders.toList()
                        .find { (key, _) -> taskInfo.isSameTo(key) }
                        ?.second?.get() ?: "tinkerId"

                    val tinkerPatchSchema = TinkerPatchSchema(
                        project = project,
                        configuration = variantTinkerPatchExtension.tinkerPatchExtension,
                        buildApkPath = newApk.absolutePath,
                        signConfig = signingConfig,
                        outputFolder = variantTinkerPatchExtension.tinkerPatchExtension.outputFolder.let { outputFolder ->
                            if (!outputFolder.isNullOrEmpty()) {
                                File(outputFolder).child {
                                    TypedValue.PATH_DEFAULT_OUTPUT / variantTinkerPatchExtension.apkVariantInfo.dirName
                                }
                            } else {
                                newApk.parentFile.parentFile.parentFile.child {
                                    TypedValue.PATH_DEFAULT_OUTPUT / variantTinkerPatchExtension.apkVariantInfo.dirName
                                }
                            }
                        }.absolutePath,
                        android = appExtension
                    )
                    Env.logger.lifecycle("tinker patch schema: $tinkerPatchSchema")
                    tinkerPatchSchema.run()
                    File("${tinkerPatchSchema.outputFolder}/patch_unsigned.apk")
                        .copyTo(buildOutputsDir.child {
                            variantTinkerPatchExtension.applicationVariant.dirName / "patch_${tinkerId}_unsigned.apk"
                        })
                    File("${tinkerPatchSchema.outputFolder}/patch_signed.apk")
                        .copyTo(buildOutputsDir.child {
                            variantTinkerPatchExtension.applicationVariant.dirName / "patch_${tinkerId}_signed.apk"
                        })
                    File("${tinkerPatchSchema.outputFolder}/patch_signed_7zip.apk")
                        .copyTo(buildOutputsDir.child {
                            variantTinkerPatchExtension.applicationVariant.dirName / "patch_${tinkerId}_signed_7zip.apk"
                        })
                }
            }
        }
    }
}

private fun reZipApkWithFilterEntry(
    apkFile: File,
    soExcludeList: List<String>,
    appExtension: AppExtension
): File {
    Env.logger.lifecycle("rezip $apkFile")
    val apkOutFile = File(apkFile.parentFile, apkFile.nameWithoutExtension + "-soExclude.apk")
    ZipOutputStream(apkOutFile.outputStream()).use { zipOutputStream ->
        ZipInputStream(apkFile.inputStream()).use { zipInput ->
            zipInput.forEach { entry ->
                if (entry.name.trim() in soExcludeList) {
                    return@forEach
                }
                zipOutputStream.putNextEntry(entry)
                zipInput.copyTo(zipOutputStream)
            }
        }
        zipOutputStream.finish()
        zipOutputStream.flush()
    }
    Env.logger.lifecycle("rezip finished: $apkOutFile")
    Thread.sleep(10000)
    // 4字节对齐
    // zipalign -v 4 cx835.apk cx835_out.apk
    val zipalign: File = appExtension.run {
        val buildToolsDir = File(File(sdkDirectory, "build-tools"), buildToolsVersion)
        if (Env.isWindows) File(buildToolsDir, "zipalign.exe")
        else File(buildToolsDir, "zipalign")
    }
    val apkAlignedFile =
        File(apkFile.parentFile, apkFile.nameWithoutExtension + "-base.apk")
    val zipalignCode = ProcessBuilder().command(
        zipalign.absolutePath,
        "-f",
        "-p",
        "-v",
        "-z",
        "4",
        apkOutFile.absolutePath,
        apkAlignedFile.absolutePath,
    ).apply {
        Env.logger.lifecycle("zipalign: " + command())
        redirectErrorStream(true)
        redirectInput(ProcessBuilder.Redirect.INHERIT)
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
        redirectError(ProcessBuilder.Redirect.INHERIT)
    }.start().waitFor()
    Env.logger.lifecycle("zipalign $apkOutFile result $zipalignCode")
    return apkAlignedFile
}

private fun signApk(
    appExtension: AppExtension,
    apkFile: File,
    signingConfigs: ApkSigningConfig,
) {
    Env.logger.lifecycle(
        "resign $apkFile ${DateFormat.getInstance().format(apkFile.lastModified())}"
    )
    val apksigner: File = appExtension.run {
        val buildToolsDir = File(File(sdkDirectory, "build-tools"), buildToolsVersion)
        if (Env.isWindows) File(buildToolsDir, "apksigner.bat")
        else File(buildToolsDir, "apksigner")
    }
    // 重签名
    val apksignerCode = ProcessBuilder().command(
        apksigner.absolutePath,
        "sign",
        "--ks",
        signingConfigs.storeFile!!.absolutePath,
        "--ks-pass",
        "pass:${signingConfigs.storePassword}",
        "--ks-key-alias",
        signingConfigs.keyAlias,
        "--key-pass",
        "pass:${signingConfigs.keyPassword}",
        apkFile.absolutePath,
    ).apply {
        redirectErrorStream(true)
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
    }.start().waitFor()
    Env.logger.lifecycle("resign $apkFile result $apksignerCode")
}

inline fun ZipInputStream.forEach(block: ZipInputStream.(entry: ZipEntry) -> Unit) {
    var zipEntry = nextEntry
    while (zipEntry != null) {
        block(zipEntry)
        closeEntry()
        zipEntry = nextEntry
    }
}
