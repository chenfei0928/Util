package io.github.chenfei0928.tinker

import com.android.build.api.dsl.SigningConfig
import com.android.build.gradle.AppExtension
import com.tencent.tinker.build.gradle.extension.TinkerPatchExtension
import com.tencent.tinker.build.patch.InputParam
import com.tencent.tinker.build.patch.Runner
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import java.io.FileFilter

/**
 * @author chenf()
 * @date 2025-12-18 16:09
 */
class TinkerPatchSchema(
    val project: Project,
    val configuration: TinkerPatchExtension,
    val buildApkPath: String,
    val signConfig: SigningConfig,
    val outputFolder: String,
    val android: AppExtension,
) {

    fun run() {
        configuration.checkParameter()
        configuration.res.checkParameter()
        configuration.dex.checkDexMode()
        configuration.sevenZip.resolveZipFinalPath()

        val builder = InputParam.Builder()
        if (configuration.useSign) {
            builder.setSignFile(signConfig.storeFile)
                .setKeypass(signConfig.keyPassword)
                .setStorealias(signConfig.keyAlias)
                .setStorepass(signConfig.storePassword)
        }

        val buildApkFile = File(buildApkPath)
        val oldApkFile = File(configuration.oldApk)
        val newApks = ArrayList<File>()
        val oldApks = ArrayList<File>()
        val oldApkNames = ArrayList<String>()
        val newApkNames = ArrayList<String>()
        if (buildApkFile.isDirectory && oldApkFile.isDirectory) {
            // Directory mode
            oldApkFile.listFiles().forEach {
                if (it.name.endsWith(".apk")) {
                    oldApks.add(it)
                    oldApkNames.add(it.name)
                }
            }
            buildApkFile.listFiles().forEach {
                if (it.name.endsWith(".apk")) {
                    newApks.add(it)
                    newApkNames.add(it.name)
                }
            }

            val unmatchedOldApkNames = HashSet(oldApkNames)
            unmatchedOldApkNames.removeAll(newApkNames)

            val unmatchedNewApkNames = HashSet(newApkNames)
            unmatchedNewApkNames.removeAll(oldApkNames)

            if (!unmatchedOldApkNames.isEmpty() || !unmatchedNewApkNames.isEmpty()) {
                throw GradleException(
                    "Both oldApk and newApk args are directories"
                            + " but apks inside them are not matched.\n"
                            + " unmatched old apks: ${unmatchedOldApkNames}\n"
                            + " unmatched new apks: ${unmatchedNewApkNames}."
                )
            }
        } else if (buildApkFile.isFile && oldApkFile.isFile) {
            // File mode
            newApks.add(buildApkFile)
            oldApks.add(oldApkFile)
        } else {
            throw GradleException("oldApk [${oldApkFile.absolutePath}] and newApk [${buildApkFile.absolutePath}] must be both files or directories.")
        }

        val tmpDir = File("${project.buildDir}/tmp/tinkerPatch")
        tmpDir.mkdirs()
        val outputDir = File(outputFolder)
        outputDir.mkdirs()

        for ((index, newApk) in newApks.withIndex()) {
            val oldApk = oldApks[index]

            val packageConfigFields =
                HashMap<String, String>(configuration.packageConfig.getFields())
            packageConfigFields.putAll(configuration.packageConfig.getApkSpecFields(newApk.name))

            builder.setOldApk(oldApk.absolutePath)
                .setNewApk(newApk.absolutePath)
                .setOutBuilder(tmpDir.absolutePath)
                .setIgnoreWarning(configuration.ignoreWarning)
                .setAllowLoaderInAnyDex(configuration.allowLoaderInAnyDex)
                .setCustomDiffPath(configuration.customPath)
                .setCustomDiffPathArgs(configuration.customDiffPathArgs)
                .setRemoveLoaderForAllDex(configuration.removeLoaderForAllDex)
                .setDexFilePattern(ArrayList<String>(configuration.dex.pattern))
                .setIsProtectedApp(configuration.buildConfig.isProtectedApp)
                .setIsComponentHotplugSupported(configuration.buildConfig.supportHotplugComponent)
                .setDexLoaderPattern(ArrayList<String>(configuration.dex.loader))
                .setDexIgnoreWarningLoaderPattern(ArrayList<String>(configuration.dex.ignoreWarningLoader))
                .setDexMode(configuration.dex.dexMode)
                .setSoFilePattern(ArrayList<String>(configuration.lib.pattern))
                .setResourceFilePattern(ArrayList<String>(configuration.res.pattern))
                .setResourceIgnoreChangePattern(ArrayList<String>(configuration.res.ignoreChange))
                .setResourceIgnoreChangeWarningPattern(ArrayList<String>(configuration.res.ignoreChangeWarning))
                .setResourceLargeModSize(configuration.res.largeModSize)
                .setUseApplyResource(configuration.buildConfig.usingResourceMapping)
                .setConfigFields(packageConfigFields)
                .setSevenZipPath(configuration.sevenZip.path)
                .setUseSign(configuration.useSign)
                .setArkHotPath(configuration.arkHot.path)
                .setArkHotName(configuration.arkHot.name)

            val inputParam = builder.create()
            Runner.gradleRun(inputParam)

            val prefix = newApk.name.take(newApk.name.lastIndexOf('.'))
            tmpDir.listFiles(FileFilter { it.isFile }).forEach {
                if (!it.name.endsWith(".apk")) {
                    return
                }
                val dest = File(outputDir, "${prefix}-${it.name}")
                it.renameTo(dest)
            }
        }
    }
}

fun <T> ArrayList(iterable: Iterable<T>): ArrayList<T> = ArrayList<T>().apply {
    addAll(iterable)
}
