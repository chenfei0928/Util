package io.github.chenfei0928.android

import io.github.chenfei0928.Contract
import io.github.chenfei0928.compiler.buildSrcKotlin
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.debugImplementation
import io.github.chenfei0928.util.implementation
import io.github.chenfei0928.util.libsForCatalog
import io.github.chenfei0928.util.library
import io.github.chenfei0928.util.version
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import kotlin.math.max

/**
 * [Document](https://developer.android.com/develop/ui/compose/documentation)
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-31 18:27
 */
fun Project.applyJetpackCompose(
    block: ComposeCompilerGradlePluginExtension.() -> Unit = {}
) {
    val libs = libsForCatalog()
    apply<ComposeCompilerGradleSubplugin>()

    buildSrcAndroid<com.android.build.gradle.BaseExtension>().apply {
        defaultConfig {
            minSdk = max(minSdk ?: 0, 21)
        }

        buildFeatures.apply {
            compose = true
        }

        compileOptions {
            sourceCompatibility = Contract.JAVA_VERSION
            targetCompatibility = Contract.JAVA_VERSION
        }
    }

    buildSrcKotlin<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(Contract.JAVA_VERSION.toString()))
        }
    }

    extensions.configure<ComposeCompilerGradlePluginExtension>("composeCompiler") {
        block()
    }

    dependencies {
        val composeBom = platform(libs.library("compose-bom").get())
        implementation(composeBom)
        androidTestImplementation(composeBom)

        implementation(libs.library("androidx-compose-material3"))
        implementation(libs.library("androidx-compose-material"))
        implementation(libs.library("androidx-compose-foundation"))
        implementation(libs.library("androidx-compose-ui"))
        implementation(libs.library("androidx-compose-ui-viewbinding"))

        implementation(libs.library("androidx-compose-ui-tooling-preview"))
        debugImplementation(libs.library("androidx-compose-ui-tooling"))

        androidTestImplementation(libs.library("androidx-compose-ui-test-junit4"))
        debugImplementation(libs.library("androidx-compose-ui-test-manifest"))

        implementation(libs.library("androidx-compose-material-icons-core"))
        implementation(libs.library("androidx-compose-material-icons-extended"))
        implementation(libs.library("androidx-compose-material3-adaptive"))

        implementation(libs.library("androidx-activity-compose"))
        implementation(libs.library("androidx-lifecycle-viewmodel-compose"))
        implementation(libs.library("androidx-compose-runtime-livedata"))

        implementation(libs.library("lottie-compose"))
        implementation(libs.library("androidx-constraintlayout-compose"))
    }
}