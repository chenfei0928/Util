package io.github.chenfei0928.data

import io.github.chenfei0928.DepsAndroidx
import io.github.chenfei0928.compiler.buildSrcKapt
import io.github.chenfei0928.compiler.buildSrcKsp
import io.github.chenfei0928.compiler.hasKotlin
import io.github.chenfei0928.compiler.hasKotlinKapt
import io.github.chenfei0928.compiler.hasKotlinKsp
import io.github.chenfei0928.util.annotationProcessor
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.implementation
import io.github.chenfei0928.util.kapt
import io.github.chenfei0928.util.ksp
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

fun Project.applyRoom() {
    val schemesPath = "$projectDir/schemas"

    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        // https://developer.android.com/training/data-storage/room/migrating-db-versions#single-migration-test
        sourceSets {
            // Adds exported schema location as test app assets.
            getByName("androidTest").assets.srcDir(schemesPath)
        }
    }

    dependencies {
        implementation(DepsAndroidx.room.runtime)
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
            ksp(DepsAndroidx.room.compiler)
            implementation(DepsAndroidx.room.ktx)
        }
    } else {
        buildSrcAndroid<com.android.build.gradle.BaseExtension> {
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
                kapt(DepsAndroidx.room.compiler)
                implementation(DepsAndroidx.room.ktx)
            } else {
                annotationProcessor(DepsAndroidx.room.compiler)
            }
        }
    }
}
