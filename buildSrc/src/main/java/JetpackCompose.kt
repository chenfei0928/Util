import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import java.lang.Integer.max

private const val composeVer = "1.0.5"

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
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        (this as org.gradle.api.plugins.ExtensionAware).extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions>(
            "kotlinOptions"
        ) {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }

        composeOptions {
            kotlinCompilerExtensionVersion = composeVer
        }
    }

    dependencies {
        implementation("androidx.compose.ui:ui:$composeVer")
        // Animations
        implementation("androidx.compose.animation:animation:$composeVer")
        // Tooling support (Previews, etc.)
        implementation("androidx.compose.ui:ui-tooling:$composeVer")
        // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
        implementation("androidx.compose.foundation:foundation:$composeVer")
        // Material Design
        implementation("androidx.compose.material:material:$composeVer")
        // Material design icons
        implementation("androidx.compose.material:material-icons-core:$composeVer")
        implementation("androidx.compose.material:material-icons-extended:$composeVer")
        // Integration with activities
        // https://dl.google.com/dl/android/maven2/androidx/activity/activity-compose/maven-metadata.xml
        implementation(DepsAndroidx.activityCompose)
        // Integration with ViewModels
        // https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-viewmodel-compose/maven-metadata.xml
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${DepsAndroidx.lifecycle.lifecycleVer}")
        // Integration with observables
        implementation("androidx.compose.runtime:runtime-livedata:$composeVer")
        implementation("androidx.compose.runtime:runtime-rxjava2:$composeVer")

        implementation(Deps.widget.lottieCompose)
        // https://dl.google.com/dl/android/maven2/androidx/constraintlayout/constraintlayout-compose/maven-metadata.xml
        implementation("androidx.constraintlayout:constraintlayout-compose:1.0.0-rc02")

        // UI Tests
        add("androidTestImplementation", "androidx.compose.ui:ui-test-junit4:$composeVer")
    }
}
