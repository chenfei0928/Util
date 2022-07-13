package io.github.chenfei0928.android

import io.github.chenfei0928.Deps
import io.github.chenfei0928.compiler.applyKotlin
import io.github.chenfei0928.compiler.hasKotlin
import io.github.chenfei0928.compiler.kapt
import io.github.chenfei0928.util.annotationProcessor
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.implementation
import io.github.chenfei0928.util.kapt
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * ARouter支持，用于设置module名给注释处理器与添加依赖
 * 必须在[applyKotlin]之后调用，且其kapt参数必须为true
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-30 17:40
 */
fun Project.applyARouter() {
    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        defaultConfig {
            javaCompileOptions {
                annotationProcessorOptions {
                    argument("AROUTER_MODULE_NAME", project.name)
                }
            }
        }
    }

    kapt {
        arguments {
            arg("AROUTER_MODULE_NAME", project.name)
        }
    }

    dependencies {
        implementation(Deps.lib.aRouter.api)
        annotationProcessor(Deps.lib.aRouter.compiler)
        if (hasKotlin) {
            kapt(Deps.lib.aRouter.compiler)
        }
    }
}
