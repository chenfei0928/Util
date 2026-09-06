package io.github.chenfei0928.bean

import com.android.build.gradle.api.ApkVariant
import com.android.builder.model.SigningConfig

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-12-10 17:11
 */
data class ApkVariantInfo(
    val name: String,
    val buildTypeName: String,
    val signingConfig: SigningConfig?,
    val dirName: String,
    val versionName: String,
    val versionCode: Int,
    val applicationId: String,
    val flavorName: String
) : java.io.Serializable {

    constructor(apkVariant: ApkVariant) : this(
        apkVariant.name,
        apkVariant.buildType.name,
        apkVariant.signingConfig,
        apkVariant.dirName,
        apkVariant.versionName,
        apkVariant.versionCode,
        apkVariant.applicationId,
        apkVariant.flavorName
    )

    companion object {
        private const val serialVersionUID = -4940583368468432371L
    }
}
