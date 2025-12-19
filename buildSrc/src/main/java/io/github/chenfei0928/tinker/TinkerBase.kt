package io.github.chenfei0928.tinker

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.VariantOutput
import com.android.build.api.variant.VariantOutputConfiguration
import com.android.build.gradle.api.BaseVariantOutput
import com.tencent.tinker.build.gradle.extension.TinkerPatchExtension
import io.github.chenfei0928.Env
import io.github.chenfei0928.bean.ApkVariantInfo
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.buildSrcAndroidComponents
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider

/**
 * 为每个编译变体创建 [TinkerPatchExtension]
 *
 * @author chenf()
 * @date 2025-12-18 15:06
 */
internal fun Project.createEveryVariantTinkerPatchExtension(): Pair<List<VariantTinkerPatchExtension>, List<String>> {
    val outputsApkPath = mutableListOf<VariantTinkerPatchExtension>()
    val buildTypeNames = mutableListOf<String>()

    buildSrcAndroid<com.android.build.gradle.AppExtension> {
        // 读取所有编译任务输出文件路径
        applicationVariants.forEach { applicationVariant ->
            applicationVariant as ExtensionAware
            val tinkerPatchExtension =
                applicationVariant.createAndConfigTinkerPatchExtension(this@createEveryVariantTinkerPatchExtension)
            applicationVariant.outputs.mapTo(outputsApkPath) {
                VariantTinkerPatchExtension(
                    applicationVariant,
                    it,
                    tinkerPatchExtension,
                    ApkVariantInfo(applicationVariant),
                )
            }
        }

        // 读取所有buildTypes
        buildTypes.forEach {
            buildTypeNames.add(it.name)
        }
    }
    return Pair(outputsApkPath, buildTypeNames)
}

data class VariantTinkerPatchExtension(
    val applicationVariant: com.android.build.gradle.api.ApplicationVariant,
    val variantOutput: BaseVariantOutput?,
    val tinkerPatchExtension: TinkerPatchExtension,
    val apkVariantInfo: ApkVariantInfo,
)

internal fun Project.putTinkerManifestPlaceholders(): Map<ApplicationVariant, Provider<String>> {
    val out = HashMap<ApplicationVariant, Provider<String>>()
    // 生成 tinkerId 到 manifestPlaceholders
    buildSrcAndroidComponents<ApplicationAndroidComponentsExtension> {
        onVariants { variant ->
            val mainOutput: VariantOutput = variant.outputs.single {
                it.outputType == VariantOutputConfiguration.OutputType.SINGLE
            }
            // 此处使用Metadata存储tinkerId，其字符串表现如果是纯数字/科学计数法/浮点Like时
            // 可能会被编译器当作是数字值而非字符串，在其字面值上加前缀规避
            val provider = provider {
                val versionName: String = mainOutput.versionName.orNull
                    ?: throw IllegalArgumentException("获取到应用版本号失败: $variant")
                // tinkerId生成规则： [versionName]_[buildConfigName]_[vcsVersionCode]_[vcsCommitId]
                versionName + "_" + variant.name + "_" + Env.vcsVersionCode + "_" + Env.vcsCommitId
            }
            variant.manifestPlaceholders.put("tinkerId", provider)
            out[variant] = provider
        }
    }
    return out
}
