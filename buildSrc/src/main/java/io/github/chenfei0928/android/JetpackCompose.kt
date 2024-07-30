package io.github.chenfei0928.android

import io.github.chenfei0928.Contract
import io.github.chenfei0928.Deps
import io.github.chenfei0928.DepsAndroidx
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.debugImplementation
import io.github.chenfei0928.util.implementation
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import java.lang.Integer.max

// Compose 与 Kotlin 的兼容性对应关系
// https://developer.android.com/jetpack/androidx/releases/compose-kotlin
private const val composeVer = "1.5.14"

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-31 18:27
 */
fun Project.applyJetpackCompose() {
    // https://developer.android.com/jetpack/compose/setup?hl=zh-cn
    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        defaultConfig {
            minSdk = max(minSdk ?: 0, 21)
        }

        buildFeatures.apply {
            // Enables Jetpack Compose for this module
            compose = true
        }

        // Set both the Java and Kotlin compilers to target Java 8.
        compileOptions {
            sourceCompatibility = Contract.JAVA_VERSION
            targetCompatibility = Contract.JAVA_VERSION
        }

        (this as ExtensionAware).extensions.configure<KotlinJvmOptions>("kotlinOptions") {
            jvmTarget = Contract.JAVA_VERSION.toString()
        }

        composeOptions {
            kotlinCompilerExtensionVersion = composeVer
        }
    }

    dependencies {
        // https://developer.android.com/jetpack/compose/bom/bom-mapping
        val composeBom = platform("androidx.compose:compose-bom:2023.08.00")
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
        implementation("androidx.compose.material3:material3-window-size-class")

        // Optional - Integration with LiveData
        implementation("androidx.compose.runtime:runtime-livedata")
        // Optional - Integration with RxJava
        implementation("androidx.compose.runtime:runtime-rxjava2")


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
