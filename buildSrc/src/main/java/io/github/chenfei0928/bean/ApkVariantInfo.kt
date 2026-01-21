package io.github.chenfei0928.bean

import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.VariantOutputConfiguration
import io.github.chenfei0928.Contract
import io.github.chenfei0928.util.AbsProvider
import io.github.chenfei0928.util.replaceFirstCharToUppercase
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-12-10 17:11
 */
data class ApkVariantInfo(
    val project: Project,
    val name: String,
    val buildTypeName: String,
    val signingConfig: com.android.build.api.variant.SigningConfig,
    val versionName: String,
    val versionCode: Int,
    val applicationId: String,
    val flavorName: String
) : java.io.Serializable {

    constructor(project: Project, apkVariant: ApplicationVariant) : this(
        project,
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

    val assembleApkTasks = arrayOf(
        AbsProvider.LazyProvider {
            project.tasks["${Contract.ASSEMBLE_TASK_PREFIX}${name.replaceFirstCharToUppercase()}"]
        },
        AbsProvider.LazyProvider { project.tasks["package${name.replaceFirstCharToUppercase()}"] },
    )

    // apk 文件生成已经由 assembleXxx 改为 packageXxx
    // https://github.com/5A59/android-training/blob/master/gradle/android_gradle_plugin-%E4%B8%BB%E8%A6%81task%E5%88%86%E6%9E%90.md
    val apkFileProvider = AbsProvider.LazyProvider {
        project.tasks["package${name.replaceFirstCharToUppercase()}"].outputs.files.asFileTree.filter {
            it.name.endsWith(".apk")
        }.singleFile
    }

    companion object {
        private const val serialVersionUID = -4940583368468432371L
    }
}
