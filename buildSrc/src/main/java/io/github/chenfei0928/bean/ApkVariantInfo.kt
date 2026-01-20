package io.github.chenfei0928.bean

import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.VariantOutputConfiguration
import io.github.chenfei0928.util.replaceFirstCharToUppercase

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-12-10 17:11
 */
data class ApkVariantInfo(
    val name: String,
    val buildTypeName: String,
    val signingConfig: com.android.build.api.variant.SigningConfig,
    val versionName: String,
    val versionCode: Int,
    val applicationId: String,
    val flavorName: String
) : java.io.Serializable {

    constructor(apkVariant: ApplicationVariant) : this(
        apkVariant.name,
        apkVariant.buildType!!,
        apkVariant.signingConfig,
        apkVariant.outputs.single {
            it.outputType == VariantOutputConfiguration.OutputType.SINGLE
        }.versionName.get(),
        apkVariant.outputs.single {
            it.outputType == VariantOutputConfiguration.OutputType.SINGLE
        }.versionCode.get(),
        apkVariant.applicationId.get(),
        apkVariant.flavorName ?: ""
    )

    val assembleTaskName: String = "assemble${name.replaceFirstCharToUppercase()}"

    companion object {
        private const val serialVersionUID = -4940583368468432371L
    }
}
