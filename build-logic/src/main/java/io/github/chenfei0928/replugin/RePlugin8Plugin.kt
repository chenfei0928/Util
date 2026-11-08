package io.github.chenfei0928.replugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.BaseExtension
import com.qihoo360.replugin.gradle.plugin.ReClassPlugin
import com.tencent.shadow.core.transform.GradleTransformWrapper
import com.tencent.shadow.core.transform_kit.AndroidClassPoolBuilder
import com.tencent.shadow.core.transform_kit.ClassPoolBuilder
import javassist.CtMethod
import javassist.Modifier
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import java.io.File

/**
 * @author chenf()
 * @date 2025-04-29 10:56
 */
class RePlugin8Plugin : Plugin<Project> {
    private lateinit var androidClassPoolBuilder: ClassPoolBuilder
    private lateinit var contextClassLoader: ClassLoader

    override fun apply(target: Project) {
        target.apply<ReClassPlugin>()

        contextClassLoader = Thread.currentThread().contextClassLoader
        val lateInitBuilder = object : ClassPoolBuilder {
            override fun build() = androidClassPoolBuilder.build().apply {
                // 追加在Host中出现但不应该在Plugin中出现的类的签名
                makeClass("com.qihoo360.replugin.component.provider.PluginProviderClient2").apply {
                    addMethod(
                        CtMethod(
                            getCtClass("android.database.Cursor"), "query", arrayOf(
                                getCtClass("android.content.Context"),
                                getCtClass("android.net.Uri"),
                                getCtClass("java.lang.String[]"),
                                getCtClass("java.lang.String"),
                                getCtClass("java.lang.String[]"),
                                getCtClass("java.lang.String"),
                            ), this
                        ).apply {
                            modifiers = Modifier.PUBLIC or Modifier.STATIC
                            setBody("{return null;}")
                        }
                    )
                    addMethod(
                        CtMethod(
                            getCtClass("android.database.Cursor"), "query", arrayOf(
                                getCtClass("android.content.Context"),
                                getCtClass("android.net.Uri"),
                                getCtClass("java.lang.String[]"),
                                getCtClass("java.lang.String"),
                                getCtClass("java.lang.String[]"),
                                getCtClass("java.lang.String"),
                                getCtClass("android.os.CancellationSignal"),
                            ), this
                        ).apply {
                            modifiers = Modifier.PUBLIC or Modifier.STATIC
                            setBody("{return null;}")
                        }
                    )
                    addMethod(
                        CtMethod(
                            getCtClass("int"), "update", arrayOf(
                                getCtClass("android.content.Context"),
                                getCtClass("android.net.Uri"),
                                getCtClass("android.content.ContentValues"),
                                getCtClass("java.lang.String"),
                                getCtClass("java.lang.String[]"),
                            ), this
                        ).apply {
                            modifiers = Modifier.PUBLIC or Modifier.STATIC
                            setBody("{return -1;}")
                        }
                    )
                }
            }
        }

        target.extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
            val transform = RePluginClassTransform(
                target, lateInitBuilder, variant.name
            )
            val taskProvider = target.tasks.register(
                "${variant.name}RePluginTransform",
                GradleTransformWrapper::class.java,
                transform
            )
            variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                .use<GradleTransformWrapper>(taskProvider)
                .toTransform(
                    ScopedArtifact.CLASSES,
                    GradleTransformWrapper::allJars,
                    GradleTransformWrapper::allDirectories,
                    GradleTransformWrapper::output
                )
        }

        val baseExtension = target.extensions.getByName("android") as BaseExtension
        target.afterEvaluate {
            initAndroidClassPoolBuilder(baseExtension, project)
        }
    }

    private fun initAndroidClassPoolBuilder(
        baseExtension: BaseExtension,
        project: Project
    ) {
        val sdkDirectory = baseExtension.sdkDirectory
        val compileSdkVersion =
            baseExtension.compileSdkVersion
                ?: throw IllegalStateException("compileSdkVersion获取失败")
        val androidJarPath = "platforms/${compileSdkVersion}/android.jar"
        val androidJar = File(sdkDirectory, androidJarPath)

        androidClassPoolBuilder = AndroidClassPoolBuilder(project, contextClassLoader, androidJar)
    }
}
