package io.github.chenfei0928.walle

import io.github.chenfei0928.bean.ApkVariantInfo
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * 某个flavor的加渠道名任务
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-12-10 14:42
 */
internal abstract class SomeoneFlavorBuildTypeChannelTask : DefaultTask() {

    @get:InputFile
    abstract val channelFile: RegularFileProperty

    @get:Input
    abstract val variant: Property<ApkVariantInfo>

    @get:InputFiles
    abstract val apkFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generateChannelApk() {
        val variant = this.variant.get()
        // 收集包信息
        val nameVariantMap = mutableMapOf<String, String?>(
            "appName" to project.name,
            "projectName" to project.rootProject.name,
            "buildType" to variant.buildTypeName,
            "versionName" to variant.versionName,
            "versionCode" to variant.versionCode.toString(),
            "packageName" to variant.applicationId,
            "flavorName" to variant.flavorName
        )
        val outputDir: File = this.outputDir.asFile.get()
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        // 读取渠道号，并生成签名后的apk包
        val channels = ChannelMaker.getChannelListFromFile(channelFile.get().asFile)
        channels.forEach {
            ChannelMaker.generateChannelApk(
                apkFile = apkFile.get().asFile,
                channelOutputFolder = outputDir,
                nameVariantMap = nameVariantMap,
                channel = it
            )
        }
    }

    fun setup() {
        description = "Make Multi-Channel by Meituan Walle"
        group = "Channel"

        onlyIf {
            val asFile: File? = channelFile.orNull?.asFile
            asFile?.exists() == true
        }
    }
}
