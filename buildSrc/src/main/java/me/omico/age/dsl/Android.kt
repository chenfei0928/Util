@file:Suppress("UnstableApiUsage", "unused")

package me.omico.age.dsl

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

fun Project.withAndroidApplication(block: Plugin<in Any>.() -> Unit) =
    plugins.withId("com.android.application", block)

fun Project.withAndroidLibrary(block: Plugin<in Any>.() -> Unit) =
    plugins.withId("com.android.library", block)

fun Project.withAndroidDynamicFeature(block: Plugin<in Any>.() -> Unit) =
    plugins.withId("com.android.dynamic-feature", block)

fun Project.withAndroid(block: Plugin<in Any>.() -> Unit) {
    withAndroidApplication(block)
    withAndroidLibrary(block)
    withAndroidDynamicFeature(block)
}

fun Project.configureAndroidCommon(block: CommonExtension<*, *, *, *>.() -> Unit) =
    withAndroid { configure("android", block) }

fun Project.withBuildType(buildType: String, block: () -> Unit) {
    if (taskRequestContains(buildType)) block()
}

fun Project.withAndroidSourcesJar() {
    configure<BaseExtension> {
        tasks.create("androidSourcesJar", Jar::class) {
            archiveClassifier.set("sources")
            from(
                sourceSets["main"].java.srcDirs,
                "src/main/kotlin",
            )
        }
    }
}
