package io.github.chenfei0928.tinker

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.tencent.tinker.build.apkparser.AndroidParser
import com.tencent.tinker.build.gradle.extension.TinkerPatchExtension
import com.tencent.tinker.build.gradle.task.TinkerPatchSchemaTask
import com.tencent.tinker.build.util.FileOperation
import com.tencent.tinker.build.util.TypedValue
import io.github.chenfei0928.Contract
import io.github.chenfei0928.Deps
import io.github.chenfei0928.bean.ApkVariantInfo
import io.github.chenfei0928.compiler.applyProguardMappingKeeping
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.checkApp
import io.github.chenfei0928.util.child
import io.github.chenfei0928.util.forEachAssembleTasks
import io.github.chenfei0928.util.implementation
import io.github.chenfei0928.util.mappingFileSaveDir
import io.github.chenfei0928.util.replaceFirstCharToUppercase
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import java.io.File
import java.util.Locale

/**
 * 接入方式为仅依赖接入，编译完手动备份R8混淆表与资源文件混淆表。
 * 在需要打补丁时，应用基线包的混淆表生成新包，并使用命令行工具生成补丁包
 *
 * @author chenfei(chenfei@cocos.com)
 * @date 2021-10-22 17:56
 */
fun Project.applyAppTinker() {
    checkApp("applyAppTinker")

    applyProguardMappingKeeping()
    applyTinkerTask()

    // tinkerSdk 所需混淆表
    buildSrcAndroid<com.android.build.gradle.AppExtension>().apply {
        defaultConfig {
            proguardFiles(projectDir.child { Contract.mappingFileSaveDirName / "tinker_proguard.pro" })
            multiDexKeepProguard = projectDir.child {
                Contract.mappingFileSaveDirName / "tinker_multidexkeep.pro"
            }
        }
    }

    // 生成 tinkerId 到 manifestPlaceholders
    putTinkerManifestPlaceholders()

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
        val (outputsApkPath, buildTypeNames) = createEveryVariantTinkerPatchExtension()

        // 根据buildTypes创建属于该buildType的全flavor的Tinker补丁包生成任务，并在之后对该project的所有task遍历中将其添加到该task的依赖中
        val patchBuildTypesTask: Map<String, TaskProvider<Task>> =
            buildTypeNames.associateWith { buildType ->
                tasks.register(
                    TINKER_PATCH_TASK_PREFIX + buildType.replaceFirstCharToUppercase()
                ) {
                    group = TASK_GROUP
                    description = TASK_DESC
                }
            }

        forEachAssembleTasks { assembleTask, taskInfo ->
            // Tinker补丁包需要文件夹配置与记录补丁信息（混淆符号映射表等）
            if (taskInfo.dimensionedFlavorName.isEmpty()) {
                return@forEachAssembleTasks
            }
            // 当前dimensionedFlavorName+buildType的基线包apk文件
            val baselineApkFile: File = project.mappingFileSaveDir.child {
                taskInfo.targetFlavorBuildTypeVariantName / "baseline.apk"
            }
            // 对当前的flavor+buildType的输出文件加渠道号
            val variantAndExtension = outputsApkPath
                .find { it.apkVariantInfo.name == taskInfo.targetFlavorBuildTypeVariantName }
                ?: throw IllegalArgumentException("没有找到 ${taskInfo.targetFlavorBuildTypeVariantName} 输出apk文件")

            variantAndExtension.tinkerPatchExtension.oldApk = baselineApkFile.absolutePath

            // 某个productFlavor-buildType的Tinker补丁包生成任务
            val tinkerPatchSomeFlavorBuildType = tasks.register<TinkerPatchSchemaTask>(
                TINKER_PATCH_TASK_PREFIX + taskInfo.targetFlavorBuildTypeVariantName.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                }) {
                group = TASK_GROUP
                description = TASK_DESC

                signConfig = variantAndExtension.apkVariantInfo.signingConfig
                this.doFirst {
                    // 读取旧包中的tinkerId
                    this@register.configuration = variantAndExtension.tinkerPatchExtension.apply {
                        buildConfig {
                            tinkerId = AndroidParser.getAndroidManifest(baselineApkFile)
                                .metaDatas[TypedValue.TINKER_ID]
                        }
                    }
                }

                setPatchNewApkPath(
                    variantAndExtension.tinkerPatchExtension,
                    variantAndExtension.variantOutput!!.outputFile,
                    variantAndExtension.apkVariantInfo
                )
                setPatchOutputFolder(
                    variantAndExtension.tinkerPatchExtension,
                    variantAndExtension.variantOutput.outputFile,
                    variantAndExtension.apkVariantInfo
                )
                // 要求该任务在标准Apk编译任务完成后进行执行
                // 使自己的assembleSomeBuildTypeChannels task依赖其(assembleTask)，并在其编译后对输出文件注入渠道号
                dependsOn(assembleTask)
                // 只有基线包存在，该task才可用
                onlyIf { baselineApkFile.exists() }
            }
            // 将该注入渠道名任务依赖到对应buildType的全渠道Task中
            patchBuildTypesTask[taskInfo.buildType]!!.dependsOn(
                tinkerPatchSomeFlavorBuildType
            )
        }
    }
}

private const val TASK_GROUP = "Patch"
private const val TASK_DESC = "Make Patch by Tencent Tinker"

internal fun TinkerPatchSchemaTask.setPatchNewApkPath(
    configuration: TinkerPatchExtension = this.configuration,
    output: File = this.buildApkPath.let(::File),
    variant: ApkVariantInfo,
) {
    val newApkPath = configuration.newApk
    if (!newApkPath.isNullOrEmpty()) {
        if (FileOperation.isLegalFileOrDirectory(newApkPath)) {
            this.buildApkPath = newApkPath
            return
        }
    }

    this.buildApkPath = output.absolutePath
}

internal fun TinkerPatchSchemaTask.setPatchOutputFolder(
    configuration: TinkerPatchExtension = this.configuration,
    output: File = this.buildApkPath.let(::File),
    variant: ApkVariantInfo,
) {
    this.outputFolder = configuration.outputFolder.let { outputFolder ->
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
