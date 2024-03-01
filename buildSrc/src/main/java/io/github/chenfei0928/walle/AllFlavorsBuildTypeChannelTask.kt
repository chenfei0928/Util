package io.github.chenfei0928.walle

import com.google.common.io.Files
import io.github.chenfei0928.bean.ApkVariantInfo
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * 全flavor包的加渠道后复制文件任务
 * 根据buildType创建该buildType的全flavor编译任务，并在之后对该project的所有task遍历中将其添加到该task的依赖中
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-12-10 16:33
 */
internal abstract class AllFlavorsBuildTypeChannelTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val outputsApkPath: Property<Collection<Pair<ApkVariantInfo, File>>>

    @get:Input
    abstract val targetBuildType: Property<String>

    @TaskAction
    fun copyAllProductFlavorsOutputFile() {
        val outputDir: File = outputDir.get().asFile
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        // 如果不只是general productFlavor，则说明是全渠道打包，将其它的productFlavors输出文件复制到最终输出目录
        outputsApkPath.get().forEach { (variant, apkFile) ->
            if (variant.buildTypeName.equals(targetBuildType.get(), true)) {
                val channels = File(apkFile.parentFile, ChannelMaker.CHANNELS_APK_OUTPUT_DIR_NAME)
                if (channels.exists()) {
                    channels.listFiles()?.forEach {
                        Files.copy(it, File(outputDir, it.name))
                    }
                } else {
                    Files.copy(apkFile, File(outputDir, apkFile.name))
                }
            }
        }
    }

    fun setup() {
        description = "Make Multi-Channel by Meituan Walle"
        group = "Channel"
    }
}
