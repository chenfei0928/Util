package io.github.chenfei0928.compiler

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import io.github.chenfei0928.Contract
import io.github.chenfei0928.Env
import io.github.chenfei0928.util.buildOutputsDir
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.buildSrcAndroidComponents
import io.github.chenfei0928.util.child
import io.github.chenfei0928.util.mappingFileSaveDir
import org.gradle.api.Project
import java.io.File
import java.util.Locale

/**
 * 保存Proguard代码混淆/aapt2资源名混淆映射表，以备在后续生成补丁包时应用
 *
 * 代码混淆映射表/资源文件名混淆映射表输出路径为：
 * `app/build/outputs/mapping/variantName/stable_res_id.txt`
 * 导入时存放到[Contract.mappingFileSaveDirName]目录下，以 variantName 作为目录名
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-01-27 14:17
 */
internal fun Project.applyProguardMappingKeeping() {
    buildSrcAndroid<com.android.build.gradle.AppExtension> {
        // dex混淆映射表保持
        productFlavors.all productFlavor@{
            // 某个flavor的混淆表导入文件夹
            Contract.minifyBuildTypes.map { buildType ->
                mappingFileSaveDir.child {
                    val upcaseBuildType = buildType.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                    }
                    "${this@productFlavor.name}${upcaseBuildType}"
                }
            }.find {
                // 如果混淆映射表存在，则生成混淆保持规则并加入，路径例如：
                // app/tinker/generalRelease/mapping.txt
                File(it, mappingKeepingFileName).exists()
            }?.let { mappingFileDir ->
                // 生成用于导入混淆表的混淆规则文件，路径例如：
                // app/tinker/generalRelease/mapping_keeping.pro
                val mappingKeepingProguardFileName: File =
                    mappingFileDir.child { "mapping_keeping.pro" }
                Env.logger.info("为 ${this.name} 应用 $mappingKeepingProguardFileName 混淆映射保持表.")
                mappingKeepingProguardFileName.let {
                    it.parentFile.mkdirs()
                    it.writeText(mappingKeepingProguardContent)
                }
                proguardFile(mappingKeepingProguardFileName)
            }
        }
    }

    // 资源id名与id值映射保持表（此映射表与 android.enableResourceOptimizations 无关）
    buildSrcAndroidComponents<ApplicationAndroidComponentsExtension> {
        onVariants { variant ->
            // 资源id固定，适用于aapt2
            // https://fucknmb.com/2017/11/15/aapt2%E9%80%82%E9%85%8D%E4%B9%8B%E8%B5%84%E6%BA%90id%E5%9B%BA%E5%AE%9A/
            // 如果该编译任务会进行混淆，保持/输出资源id映射表
            if (Contract.minifyBuildTypes.find {
                    variant.name.contains(it, true)
                } != null) {
                // 资源id保持表，路径例如：
                // app/tinker/generalRelease/stable_res_id.txt
                val publicTxtFile: File = mappingFileSaveDir.child {
                    variant.name / "stable_res_id.txt"
                }
                if (publicTxtFile.exists()) {
                    // 如果资源id保持表存在，应用该保持表
                    Env.logger.info("为 ${variant.name} 应用 $publicTxtFile 资源ID保持表.")
                    variant.androidResources.aaptAdditionalParameters.addAll(
                        "--stable-ids", publicTxtFile.absolutePath
                    )
                }
                // 无论资源id保持表是否存在，都将资源id保持表文件输出到如下文件中，路径例如：
                // app/build/outputs/mapping/generalRelease/stable_res_id.txt
                val publicTxtOutFile = buildOutputsDir.child {
                    "mapping" / variant.name / "stable_res_id.txt"
                }
                publicTxtOutFile.parentFile.mkdirs()
                variant.androidResources.aaptAdditionalParameters.addAll(
                    "--emit-ids", publicTxtOutFile.absolutePath
                )
            }
        }
    }
}

internal val Project.containsMappingKeepingFile: Boolean
    get() {
        return !mappingFileSaveDir.listFiles { dir: File ->
            Contract.minifyBuildTypes.find {
                dir.name.endsWith(it, true)
            } != null
                    && dir.isDirectory
                    && File(dir, mappingKeepingFileName).exists()
        }.isNullOrEmpty()
    }

private const val mappingKeepingFileName = "mapping.txt"
private const val mappingKeepingProguardContent = """
# 由buildSrc中ProguardMappingKeeping.kt文件自动生成
# 应用混淆映射表
-applymapping $mappingKeepingFileName
"""
