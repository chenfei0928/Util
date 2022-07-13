package io.github.chenfei0928.android

import io.github.chenfei0928.util.buildSrcAndroid
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-28 17:37
 */
fun Project.applyTest() {
    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        defaultConfig {
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            setTestInstrumentationRunnerArguments(
                mutableMapOf(
                    "clearPackageData" to "true"
                )
            )
        }

        testOptions {
            unitTests.isIncludeAndroidResources = true

            execution = "ANDROIDX_TEST_ORCHESTRATOR"
        }

        useLibrary("android.test.runner")
        useLibrary("android.test.base")
        useLibrary("android.test.mock")
    }

    dependencies {
        // Unit testing dependencies.
        // https://github.com/junit-team/junit5/
        testImplementation("junit:junit:4.13.2")
        // https://github.com/mockito/mockito
        testImplementation("org.mockito:mockito-core:2.19.0")
        // https://developer.android.com/training/testing/junit-runner
        androidTestImplementation("androidx.test:runner:1.3.0")
        add("androidTestUtil", "androidx.test:orchestrator:1.3.0")

        // https://developer.android.com/training/testing/set-up-project#gradle-maven-dependencies
        // Core library
        androidTestImplementation("androidx.test:core:1.3.0")

        // AndroidJUnitRunner and JUnit Rules
        androidTestImplementation("androidx.test:runner:1.3.0")
        androidTestImplementation("androidx.test:rules:1.3.0")

        // Assertions
        androidTestImplementation("androidx.test.ext:junit:1.1.2")
        androidTestImplementation("androidx.test.ext:truth:1.3.0")
        androidTestImplementation("com.google.truth:truth:1.0")

        // Espresso dependencies
        androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
        androidTestImplementation("androidx.test.espresso:espresso-contrib:3.3.0")
        androidTestImplementation("androidx.test.espresso:espresso-intents:3.3.0")
        androidTestImplementation("androidx.test.espresso:espresso-accessibility:3.3.0")
        androidTestImplementation("androidx.test.espresso:espresso-web:3.3.0")
        androidTestImplementation("androidx.test.espresso.idling:idling-concurrent:3.3.0")

        // The following Espresso dependency can be either "implementation"
        // or "androidTestImplementation", depending on whether you want the
        // dependency to appear on your APK's compile classpath or the test APK
        // classpath.
        androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.3.0")
    }
}

private fun DependencyHandlerScope.androidTestImplementation(dependencyNotation: String) {
    add("androidTestImplementation", dependencyNotation)
}

private fun DependencyHandlerScope.testImplementation(dependencyNotation: String) {
    add("testImplementation", dependencyNotation)
}
