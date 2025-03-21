package io.github.chenfei0928.tinker

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.VariantOutput
import com.android.build.api.variant.VariantOutputConfiguration
import com.tencent.tinker.build.apkparser.AndroidParser
import com.tencent.tinker.build.gradle.extension.TinkerPatchExtension
import com.tencent.tinker.build.gradle.task.TinkerPatchSchemaTask
import com.tencent.tinker.build.util.FileOperation
import com.tencent.tinker.build.util.TypedValue
import io.github.chenfei0928.Contract
import io.github.chenfei0928.Deps
import io.github.chenfei0928.Env
import io.github.chenfei0928.bean.ApkVariantInfo
import io.github.chenfei0928.compiler.applyProguardMappingKeeping
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.buildSrcAndroidComponents
import io.github.chenfei0928.util.checkApp
import io.github.chenfei0928.util.child
import io.github.chenfei0928.util.forEachAssembleTasks
import io.github.chenfei0928.util.implementation
import io.github.chenfei0928.util.mappingFileSaveDir
import io.github.chenfei0928.util.replaceFirstCharToUppercase
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import java.io.File

// tinker混淆文件存放位置
internal const val TINKER_CONFIG_DIR = "tinker"

/**
 * 接入方式为仅依赖接入，编译完手动备份R8混淆表与资源文件混淆表。
 * 在需要打补丁时，应用基线包的混淆表生成新包，并使用命令行工具生成补丁包
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-10-22 17:56
 */
fun Project.applyAppTinker() {
    checkApp("applyAppTinker")

    applyProguardMappingKeeping()
    applyTinkerTask()

    // tinkerSdk 所需混淆表
    buildSrcAndroid<com.android.build.gradle.AppExtension> {
        defaultConfig {
            proguardFiles(projectDir.child { TINKER_CONFIG_DIR / "tinker_proguard.pro" })
            multiDexKeepProguard = projectDir.child {
                TINKER_CONFIG_DIR / "tinker_multidexkeep.pro"
            }
        }
    }

    // 生成 tinkerId 到 manifestPlaceholders
    buildSrcAndroidComponents<ApplicationAndroidComponentsExtension> {
        onVariants { variant ->
            val mainOutput: VariantOutput = variant.outputs.single {
                it.outputType == VariantOutputConfiguration.OutputType.SINGLE
            }
            // 此处使用Metadata存储tinkerId，其字符串表现如果是纯数字/科学计数法/浮点Like时
            // 可能会被编译器当作是数字值而非字符串，在其字面值上加前缀规避
            variant.manifestPlaceholders.put("tinkerId", provider {
                val channel: String = variant.buildConfigFields.getting("channel").get().run {
                    if (type == "String") {
                        // 字符串需要去掉两侧的引号
                        value.toString().substring(1, value.toString().length - 1)
                    } else {
                        value.toString()
                    }
                }
                val versionName: String = mainOutput.versionName.orNull
                    ?: throw IllegalArgumentException("获取到应用版本号失败")
                val campaignName: String = variant.productFlavors
                    .find { it.first == Contract.flavorDimensions_campaignName }
                    ?.second
                    ?: throw IllegalArgumentException("找不到推广标识 campaignName")
                // tinkerId生成规则：[channel]_[versionName]_[campaignName]_[codeVersionInt]
                channel + "_" + versionName + "_" + campaignName + "_" + Env.vcsVersionCode
            })
        }
    }

    // 添加 tinkerSdk 依赖
    dependencies {
        implementation(Deps.framework.tinker)
    }
}

/**
 * 生成的补丁包生成任务的任务名前缀，任务名命名规则为
 *
 * ```assemble[DimensionedFlavorName][BuildType]```
 */
private const val TINKER_PATCH_TASK_PREFIX = "tinkerPatch"

private fun Project.applyTinkerTask() {
    applyTinkerPluginConfig(withPlugin = false)

    afterEvaluate {
        val outputsApkPath = mutableListOf<Triple<ApkVariantInfo, File, TinkerPatchExtension>>()
        val buildTypeNames = mutableListOf<String>()

        buildSrcAndroid<com.android.build.gradle.AppExtension> {
            // 读取所有编译任务输出文件路径
            applicationVariants.forEach { applicationVariant ->
                applicationVariant as ExtensionAware
                val tinkerPatchExtension =
                    applicationVariant.createAndConfigTinkerPatchExtension(this@applyTinkerTask)
                applicationVariant.outputs.mapTo(outputsApkPath) {
                    Triple(ApkVariantInfo(applicationVariant), it.outputFile, tinkerPatchExtension)
                }
            }

            // 读取所有buildTypes
            buildTypes.forEach {
                buildTypeNames.add(it.name)
            }
        }

        // 根据buildTypes创建属于该buildType的全flavor的Tinker补丁包生成任务，并在之后对该project的所有task遍历中将其添加到该task的依赖中
        val patchBuildTypesTask: Map<String, Task> = buildTypeNames.associateWith { buildType ->
            return@associateWith task(TINKER_PATCH_TASK_PREFIX + buildType.replaceFirstCharToUppercase()) {
                description = "Make Patch by Tencent Tinker"
                group = "Patch"
            }
        }

        forEachAssembleTasks { assembleTask, taskInfo ->
            if (taskInfo.dimensionedFlavorName.isNotEmpty()) {
                // 当前dimensionedFlavorName+buildType的基线包apk文件
                val baselineApkFile: File = project.mappingFileSaveDir.child {
                    taskInfo.targetFlavorBuildTypeVariantName / "baseline.apk"
                }
                // 对当前的flavor+buildType的输出文件加渠道号
                val (variant, targetFlavorBuildTypeApkFile, tinkerPatchExtension) = outputsApkPath
                    .find { (variant, _) ->
                        variant.name == taskInfo.targetFlavorBuildTypeVariantName
                    }
                    ?: throw IllegalArgumentException("没有找到 ${taskInfo.targetFlavorBuildTypeVariantName} 输出apk文件")

                tinkerPatchExtension.oldApk = baselineApkFile.absolutePath

                // 某个productFlavor-buildType的Tinker补丁包生成任务
                val tinkerPatchSomeFlavorBuildType: Task = tasks.create<TinkerPatchSchemaTask>(
                    TINKER_PATCH_TASK_PREFIX +
                            taskInfo.dimensionedFlavorBuildTypeName
                ).apply {
                    description = "Make Patch by Tencent Tinker"
                    group = "Patch"

                    signConfig = variant.signingConfig
                    this.doFirst {
                        // 读取旧包中的tinkerId
                        this@apply.configuration = tinkerPatchExtension.apply {
                            buildConfig {
                                tinkerId = AndroidParser.getAndroidManifest(baselineApkFile)
                                    .metaDatas[TypedValue.TINKER_ID]
                            }
                        }
                    }

                    setPatchNewApkPath(
                        tinkerPatchExtension, targetFlavorBuildTypeApkFile, variant, this
                    )
                    setPatchOutputFolder(
                        tinkerPatchExtension, targetFlavorBuildTypeApkFile, variant, this
                    )
                }
                // 要求该任务在标准Apk编译任务完成后进行执行
                // 使自己的assembleSomeBuildTypeChannels task依赖其(assembleTask)，并在其编译后对输出文件注入渠道号
                tinkerPatchSomeFlavorBuildType.dependsOn(assembleTask)
                // 只有基线包存在，该task才可用
                tinkerPatchSomeFlavorBuildType.onlyIf {
                    baselineApkFile.exists()
                }
                // 将该注入渠道名任务依赖到对应buildType的全渠道Task中
                patchBuildTypesTask[taskInfo.buildType]!!.dependsOn(
                    tinkerPatchSomeFlavorBuildType
                )
            }
        }
    }
}

private fun setPatchNewApkPath(
    configuration: TinkerPatchExtension,
    output: File,
    variant: ApkVariantInfo,
    tinkerPatchBuildTask: TinkerPatchSchemaTask
) {
    val newApkPath = configuration.newApk
    if (!newApkPath.isNullOrEmpty()) {
        if (FileOperation.isLegalFileOrDirectory(newApkPath)) {
            tinkerPatchBuildTask.buildApkPath = newApkPath
            return
        }
    }

    tinkerPatchBuildTask.buildApkPath = output.absolutePath
}

private fun setPatchOutputFolder(
    configuration: TinkerPatchExtension,
    output: File,
    variant: ApkVariantInfo,
    tinkerPatchBuildTask: TinkerPatchSchemaTask
) {
    tinkerPatchBuildTask.outputFolder = configuration.outputFolder.let { outputFolder ->
        if (!outputFolder.isNullOrEmpty()) {
            File(outputFolder).child {
                TypedValue.PATH_DEFAULT_OUTPUT / variant.dirName
            }
        } else {
            output.parentFile.parentFile.parentFile.child {
                TypedValue.PATH_DEFAULT_OUTPUT / variant.dirName
            }
        }
    }.absolutePath
}
