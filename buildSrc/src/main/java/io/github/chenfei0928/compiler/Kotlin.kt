package io.github.chenfei0928.compiler

import io.github.chenfei0928.Deps
import io.github.chenfei0928.Env
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.implementation
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin
import org.jetbrains.kotlin.gradle.internal.ParcelizeSubplugin
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

/**
 * Kotlin语言、反射、协程配置
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-11-09 14:35
 */
fun Project.applyKotlin(
    parcelize: Boolean = false,
    kapt: Boolean = false
) {
    apply<KotlinAndroidPluginWrapper>()
    if (parcelize) {
        apply<ParcelizeSubplugin>()
    }
    if (kapt) {
        apply<Kapt3GradleSubplugin>()
    }

    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        (this as ExtensionAware).extensions.configure<KotlinJvmOptions>("kotlinOptions") {
            jvmTarget = JavaVersion.VERSION_1_8.toString()

            // Kotlin编译选项，可使用 kotlinc -X 查看
            // https://droidyue.com/blog/2019/07/21/configure-kotlin-compiler-options/
            if (Env.containsReleaseBuild) {
                // Release编译时禁止参数非空检查
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-Xno-param-assertions", "-Xno-receiver-assertions"
                )
            }
        }
    }

    val kotlinPluginVersion = getKotlinPluginVersion()

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinPluginVersion")
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinPluginVersion")
        implementation(Deps.kotlin.coroutines)
        // Parcelable序列化支持
        implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:$kotlinPluginVersion")
    }
}

internal val Project.hasKotlin: Boolean
    get() = plugins.hasPlugin(KotlinAndroidPluginWrapper::class.java)

internal fun Project.kapt(block: KaptExtension.() -> Unit) {
    if (!hasKotlin) {
        return
    }
    if (!plugins.hasPlugin(Kapt3GradleSubplugin::class.java)) {
        apply<Kapt3GradleSubplugin>()
    }
    extensions.configure("kapt", block)
}
