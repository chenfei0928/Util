package io.github.chenfei0928.data

import com.android.build.api.dsl.CommonExtension
import io.github.chenfei0928.compiler.buildSrcKapt
import io.github.chenfei0928.compiler.buildSrcKsp
import io.github.chenfei0928.compiler.hasKotlin
import io.github.chenfei0928.compiler.hasKotlinKapt
import io.github.chenfei0928.compiler.hasKotlinKsp
import io.github.chenfei0928.util.annotationProcessor
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.defaultConfig
import io.github.chenfei0928.util.implementation
import io.github.chenfei0928.util.kapt
import io.github.chenfei0928.util.ksp
import io.github.chenfei0928.util.sourceSets
import io.github.chenfei0928.util.libsForCatalog
import io.github.chenfei0928.util.library
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

fun Project.applyRoom() {
    val libs = libsForCatalog()
    val schemesPath = "$projectDir/schemas"

    buildSrcAndroid<CommonExtension>().apply {
        // https://developer.android.com/training/data-storage/room/migrating-db-versions#single-migration-test
        sourceSets {
            // Adds exported schema location as test app assets.
            getByName("androidTest").assets.directories.add(schemesPath)
        }
    }

    dependencies {
        implementation(libs.library("androidx-room-runtime"))
    }

    val param = mapOf(
        "room.schemaLocation" to schemesPath,
        "room.incremental" to "true",
    )

    if (hasKotlinKsp) {
        buildSrcKsp {
            param.forEach { (k, value) -> arg(k, value) }
        }

        dependencies {
            ksp(libs.library("androidx-room-compiler"))
            implementation(libs.library("androidx-room-ktx"))
        }
    } else {
        buildSrcAndroid<CommonExtension>().apply {
            defaultConfig {
                //指定room.schemaLocation生成的文件路径
                javaCompileOptions {
                    annotationProcessorOptions {
                        arguments(param)
                    }
                }
            }

            if (hasKotlinKapt) {
                buildSrcKapt {
                    arguments {
                        param.forEach { (k, v) -> arg(k, v) }
                    }
                }
            }
        }

        dependencies {
            if (hasKotlin) {
                kapt(libs.library("androidx-room-compiler"))
                implementation(libs.library("androidx-room-ktx"))
            } else {
                annotationProcessor(libs.library("androidx-room-compiler"))
            }
        }
    }
}
