package io.github.chenfei0928.android

import io.github.chenfei0928.DepsAndroidx
import io.github.chenfei0928.compiler.kotlinPluginVersion
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.debugImplementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-28 17:37
 */
fun Project.applyTest() {
    buildSrcAndroid<com.android.build.gradle.BaseExtension>().apply {
        defaultConfig {
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            // The following argument makes the Android Test Orchestrator run its
            // "pm clear" command after each test invocation. This command ensures
            // that the app's state is completely cleared between tests.
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
        // https://github.com/mockito/mockito
        testImplementation("org.mockito:mockito-core:5.17.0")
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
        // Testing Fragments in Isolation
        debugImplementation(DepsAndroidx.fragmentTest)
        testImplementation(DepsAndroidx.arch.testing)
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
        // https://github.com/andrzejchm/RESTMock
        testImplementation("com.github.andrzejchm.RESTMock:android:0.4.4")
        //AssertJ
        testImplementation("org.assertj:assertj-core:3.13.2")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinPluginVersion")

        // powermock
        // https://github.com/powermock/powermock
        testImplementation("org.powermock:powermock-module-junit4:2.0.9")
        testImplementation("org.powermock:powermock-module-junit4-rule:2.0.9")
        testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
        testImplementation("org.powermock:powermock-classloading-xstream:2.0.9")
        //mockwebserver
        testImplementation("com.squareup.okhttp3:mockwebserver:3.12.0")

        // https://developer.android.com/training/testing/junit-runner
        androidTestImplementation("androidx.test:runner:1.3.0")
        add("androidTestUtil", "androidx.test:orchestrator:1.3.0")

        // Robolectric：本地单元测试依赖 Android 框架(主要应用于UI测试)
        testImplementation("org.robolectric:robolectric:4.13")

        // https://mockk.io/
        val mockkVersion = "1.14.0"
        //main mockk
        testImplementation("io.mockk:mockk:$mockkVersion")
        //Unit
        testImplementation("io.mockk:mockk-android:$mockkVersion")
        testImplementation("io.mockk:mockk-agent:$mockkVersion")
        //Instrumented
        androidTestImplementation("io.mockk:mockk-android:$mockkVersion")
        androidTestImplementation("io.mockk:mockk-agent:$mockkVersion")

        // https://developer.android.com/training/testing/set-up-project#gradle-maven-dependencies
        // https://developer.android.com/jetpack/androidx/releases/test
        // Core library
        androidTestImplementation("androidx.test:core:1.6.1")

        // AndroidJUnitRunner and JUnit Rules
        androidTestImplementation("androidx.test:runner:1.6.2")
        androidTestImplementation("androidx.test:rules:1.6.1")

        // Assertions
        androidTestImplementation("androidx.test.ext:junit:1.2.1")
        androidTestImplementation("androidx.test.ext:truth:1.6.0")

        // Espresso dependencies
        val espressoVersion = "3.6.1"
        androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
        androidTestImplementation("androidx.test.espresso:espresso-contrib:$espressoVersion")
        androidTestImplementation("androidx.test.espresso:espresso-intents:$espressoVersion")
        androidTestImplementation("androidx.test.espresso:espresso-accessibility:$espressoVersion")
        androidTestImplementation("androidx.test.espresso:espresso-web:$espressoVersion")
        androidTestImplementation("androidx.test.espresso.idling:idling-concurrent:$espressoVersion")

        // The following Espresso dependency can be either "implementation",
        // or "androidTestImplementation", depending on whether you want the
        // dependency to appear on your APK"s compile classpath or the test APK
        // classpath.
        androidTestImplementation("androidx.test.espresso:espresso-idling-resource:$espressoVersion")
    }
}

internal fun DependencyHandlerScope.androidTestImplementation(dependencyNotation: Any) {
    add("androidTestImplementation", dependencyNotation)
}

internal fun DependencyHandlerScope.testImplementation(dependencyNotation: Any) {
    add("testImplementation", dependencyNotation)
}
