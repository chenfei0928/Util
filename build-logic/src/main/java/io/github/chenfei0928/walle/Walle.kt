package io.github.chenfei0928.walle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.google.common.io.Files
import com.meituan.android.walle.GradlePlugin
import io.github.chenfei0928.Contract
import io.github.chenfei0928.Env
import io.github.chenfei0928.bean.ApkVariantInfo
import io.github.chenfei0928.util.AbsProvider
import io.github.chenfei0928.util.buildOutputsDir
import io.github.chenfei0928.util.buildSrcAndroidComponents
import io.github.chenfei0928.util.checkApp
import io.github.chenfei0928.util.child
import io.github.chenfei0928.util.forEachAssembleTasks
import io.github.chenfei0928.util.implementation
import io.github.chenfei0928.util.replaceFirstCharToUppercase
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.dependencies
import org.joor.Reflect
import java.io.File

/**
 * 使用打包脚本CLI进行处理，此处不依赖plugin
 * 美团官方维护的版本不支持V3签名，使用的版本为：https://github.com/Meituan-Dianping/walle/issues/264
 *
 * 生成签名任务可以参考[com.meituan.android.walle.GradlePlugin.applyTask]和
 * [com.meituan.android.walle.ChannelMaker.generateChannelApk]
 * walle
 *
 * @date 2021-11-16 15:00
 */
fun Project.applyAppWalle() {
    checkApp("applyAppWalle")

    val walleVersion: String = Reflect.onClass(GradlePlugin::class.java)
        .call("getVersion").get() ?: "1.0.5"

    // 自处理打渠道号流程，不使用Plugin处理，以避免引入了其他Plugin后无法共存
    // 也不使用CLI，减少打包流程人工操作的量或额外编写打包后处理脚本
    // 美团官方维护的版本不支持V3签名，使用的版本为：https://github.com/Meituan-Dianping/walle/issues/264
    dependencies {
        // https://github.com/Meituan-Dianping/walle
        implementation("com.github.Petterpx.walle:library:$walleVersion")
//        implementation("com.meituan.android.walle:library:$walleVersion")
    }

    // 读取所有编译任务输出文件路径
    val outputsApkPath: MutableList<ApkVariantInfo> = ArrayList()
    val appExt = buildSrcAndroidComponents<ApplicationAndroidComponentsExtension>()
    // 读取所有buildTypes
    val buildTypeNames = ArrayList<String>()
    appExt.finalizeDsl {
        buildTypeNames.addAll(it.buildTypes.map { it.name })
    }
    appExt.onVariants { variant ->
        outputsApkPath.add(ApkVariantInfo(this, variant))
    }

    // 此时主build.gradle.kts还未执行完毕，等待project configure完毕后，根据生成的编译任务添加渠道信息注入task
    afterEvaluate {
        Env.logger.lifecycle("Generate Walle package task for every variants: $outputsApkPath $buildTypeNames")
        // 根据buildTypes创建属于该buildType的全flavor编译任务，并在之后对该project的所有task遍历中将其添加到该task的依赖中
        val buildTypesAllFlavorTask = buildTypeNames.associateWith { buildType ->
            return@associateWith tasks.register(
                Contract.ASSEMBLE_TASK_PREFIX + buildType.replaceFirstCharToUppercase() + MAKE_CHANNEL_TASK_SUFFIX
            ) {
                outputs.dir(buildOutputsDir.child {
                    CHANNELS_APK_OUTPUT_DIR_NAME / buildType
                })
                description = "Make Multi-Channel by Meituan Walle"
                group = "Channel"

                doLast {
                    val outputDir: File = outputs.files.singleFile
                    if (!outputDir.exists()) {
                        outputDir.mkdirs()
                    }
                    // 如果不只是general productFlavor，则说明是全渠道打包，将其它的productFlavors输出文件复制到最终输出目录
                    outputsApkPath.forEach { variant ->
                        if (variant.buildTypeName.equals(buildType, true)) {
                            val apkFile = variant.apkFileProvider.get()
                            val channels = File(
                                apkFile, ChannelMaker.CHANNELS_APK_OUTPUT_DIR_NAME
                            )
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
            }
        }

        forEachAssembleTasks { assembleTask, taskInfo ->
            if (taskInfo.dimensionedFlavorName.isNotEmpty()) {
                // 当前dimensionedFlavorName的渠道信息文件，保存在 [app]/channels/[flavorName] 文件中
                val channelFile: File = project.projectDir.child {
                    CHANNELS_PROFILE_DIR / taskInfo.dimensionedFlavorName
                }
                // 对当前的flavor+buildType的输出文件加渠道号
                val variant = outputsApkPath
                    .find { variant -> variant.name == taskInfo.targetFlavorBuildTypeVariantName }
                    ?: throw IllegalArgumentException(
                        "not found ${taskInfo.targetFlavorBuildTypeVariantName} output apk file in $outputsApkPath"
                    )

                // 某个productFlavor-buildType的渠道包任务
                val assembleSomeBuildTypeChannels: TaskProvider<*> =
                    tasks.register(assembleTask.name + MAKE_CHANNEL_TASK_SUFFIX) {
                        inputs.file(channelFile)
                        outputs.dir(AbsProvider.LazyProvider {
                            File(
                                variant.apkFileProvider.get().parentFile,
                                CHANNELS_APK_OUTPUT_DIR_NAME
                            )
                        })
                        description = "Make Multi-Channel by Meituan Walle"
                        group = "Channel"

                        // 要求该任务在标准Apk编译任务完成后进行执行
                        // 使自己的assembleSomeBuildTypeChannels task依赖其(assembleTask)，并在其编译后对输出文件注入渠道号
                        dependsOn(assembleTask)
                        dependsOn(*variant.assembleApkTasks)
                        // 只有渠道信息文件存在，该task才可用
                        onlyIf { channelFile.exists() }

                        doLast {
                            // 收集包信息
                            val nameVariantMap = mutableMapOf<String, String?>(
                                "appName" to project.name,
                                "projectName" to project.rootProject.name,
                                "buildType" to variant.buildTypeName,
                                "versionName" to variant.versionName,
                                "versionCode" to variant.versionCode.toString(),
                                "packageName" to variant.applicationId,
                                "flavorName" to variant.flavorName,
                            )
                            val apkFile = variant.apkFileProvider.get()
                            Env.logger.lifecycle("Inject channels for ${taskInfo}: $apkFile")
                            val outputDir: File = outputs.files.singleFile
                            if (!outputDir.exists()) {
                                outputDir.mkdirs()
                            }

                            // 读取渠道号，并生成签名后的apk包
                            ChannelMaker.getChannelListFromFile(channelFile).forEach {
                                ChannelMaker.generateChannelApk(
                                    apkFile = apkFile,
                                    channelOutputFolder = outputDir,
                                    nameVariantMap = nameVariantMap,
                                    channel = it,
                                )
                            }
                        }
                    }
                // 将该注入渠道名任务依赖到对应buildType的全渠道Task中
                buildTypesAllFlavorTask[taskInfo.buildType]?.configure {
                    dependsOn(assembleSomeBuildTypeChannels)
                }
            }
        }
    }
}

/**
 * 打入渠道的文件的输出文件夹名
 */
private const val CHANNELS_APK_OUTPUT_DIR_NAME = ChannelMaker.CHANNELS_APK_OUTPUT_DIR_NAME

/**
 * 打入渠道号的任务名后缀，打渠道包任务命名规则为
 * ```assemble[DimensionedFlavorName][BuildType]Channels```
 */
private const val MAKE_CHANNEL_TASK_SUFFIX = "Channels"

/**
 * 渠道配置目录名，以flavor名创建文件，内配置要打入的渠道名列表
 */
private const val CHANNELS_PROFILE_DIR = "channels"
