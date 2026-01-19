package io.github.chenfei0928.android

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.BuildFeatures
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DynamicFeatureExtension
import com.android.build.api.dsl.LibraryExtension
import io.github.chenfei0928.Contract
import io.github.chenfei0928.Deps
import io.github.chenfei0928.DepsAndroidx
import io.github.chenfei0928.compiler.buildSrcKotlin
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.debugImplementation
import io.github.chenfei0928.util.implementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * [Document](https://developer.android.com/develop/ui/compose/documentation)
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-31 18:27
 */
fun Project.applyJetpackCompose(
    block: ComposeCompilerGradlePluginExtension.() -> Unit = {}
) {
    apply<ComposeCompilerGradleSubplugin>()

    fun BuildFeatures.apply() {
        // Enables Jetpack Compose for this module
        compose = true
    }

    // https://developer.android.com/jetpack/compose/setup?hl=zh-cn
    when (val ext = buildSrcAndroid<CommonExtension>()) {
        is ApplicationExtension -> ext.apply {
            buildFeatures.apply()
        }
        is LibraryExtension -> ext.apply {
            buildFeatures.apply()
        }
        is DynamicFeatureExtension -> ext.apply {
            buildFeatures.apply()
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
        // https://developer.android.com/jetpack/compose/bom/bom-mapping
        val composeBom = platform("androidx.compose:compose-bom:2025.12.00")
        implementation(composeBom)
        androidTestImplementation(composeBom)

        // Choose one of the following:
        // Material Design 3
        implementation("androidx.compose.material3:material3")
        // or Material Design 2
        implementation("androidx.compose.material:material")
        // or skip Material Design and build directly on top of foundational components
        implementation("androidx.compose.foundation:foundation")
        // or only import the main APIs for the underlying toolkit systems,
        // such as input and measurement/layout
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.ui:ui-viewbinding")

        // Android Studio Preview support
        implementation("androidx.compose.ui:ui-tooling-preview")
        debugImplementation("androidx.compose.ui:ui-tooling")

        // UI Tests
        androidTestImplementation("androidx.compose.ui:ui-test-junit4")
        debugImplementation("androidx.compose.ui:ui-test-manifest")

        // Optional - Included automatically by material, only add when you need
        // the icons but not the material library (e.g. when using Material3 or a
        // custom design system based on Foundation)
        implementation("androidx.compose.material:material-icons-core")
        // Optional - Add full set of material icons
        implementation("androidx.compose.material:material-icons-extended")
        // Optional - Add window size utils
        implementation("androidx.compose.material3.adaptive:adaptive")

        // Optional - Integration with activities
        implementation("androidx.activity:activity-compose")
        // Optional - Integration with ViewModels
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
        // Optional - Integration with LiveData
        implementation("androidx.compose.runtime:runtime-livedata")

        // Integration with activities
        // https://dl.google.com/dl/android/maven2/androidx/activity/activity-compose/maven-metadata.xml
        implementation(DepsAndroidx.activityCompose)
        // Integration with ViewModels
        // https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-viewmodel-compose/maven-metadata.xml
        implementation(DepsAndroidx.lifecycle.jetpackCompose)

        implementation(Deps.widget.lottieCompose)
        // https://dl.google.com/dl/android/maven2/androidx/constraintlayout/constraintlayout-compose/maven-metadata.xml
        implementation(DepsAndroidx.constraintlayout.compose)
    }
}
