package me.omico.age.dsl

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure

fun Project.withJava(block: Plugin<in Any>.() -> Unit) =
    plugins.withId("java", block)

fun Project.javaCompatibility(
    source: JavaVersion,
    target: JavaVersion,
) {
    withJava {
        configure<JavaPluginExtension> {
            sourceCompatibility = source
            targetCompatibility = target
        }
    }
}

fun Project.javaCompatibility(
    all: JavaVersion,
) = javaCompatibility(
    source = all,
    target = all,
)

fun Project.withJavaSourcesJar() {
    withJava {
        configure<JavaPluginExtension> {
            withSourcesJar()
        }
    }
}
