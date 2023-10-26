package io.github.chenfei0928.android

import com.alibaba.android.arouter.register.launch.PluginLaunch
import io.github.chenfei0928.Deps
import io.github.chenfei0928.compiler.applyKotlin
import io.github.chenfei0928.compiler.buildSrcKapt
import io.github.chenfei0928.compiler.hasKotlin
import io.github.chenfei0928.util.annotationProcessor
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.implementation
import io.github.chenfei0928.util.kapt
import io.github.chenfei0928.util.writeTmpProguardFile
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

/**
 * ARouter支持，用于设置module名给注释处理器与添加依赖
 * 必须在[applyKotlin]之后调用，且其kapt参数必须为true
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-30 17:40
 */
fun Project.applyARouter() {
    if (plugins.hasPlugin(com.android.build.gradle.AppPlugin::class.java)) {
        apply<PluginLaunch>()
    }

    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        defaultConfig {
            proguardFile(writeTmpProguardFile("aRouter.pro", proguardContent))

            javaCompileOptions {
                annotationProcessorOptions {
                    argument("AROUTER_MODULE_NAME", project.name)
                }
            }
        }
    }

    if (hasKotlin) {
        buildSrcKapt {
            arguments {
                arg("AROUTER_MODULE_NAME", project.name)
            }
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

private val proguardContent = """
-keep public class com.alibaba.android.arouter.routes.**{*;}
-keep public class com.alibaba.android.arouter.facade.**{*;}
-keep class * implements com.alibaba.android.arouter.facade.template.ISyringe{*;}

# 如果使用了 byType 的方式获取 Service，需添加下面规则，保护接口
-keep interface * implements com.alibaba.android.arouter.facade.template.IProvider

# 如果使用了 单类注入，即不定义接口实现 IProvider，需添加下面规则，保护实现
# -keep class * implements com.alibaba.android.arouter.facade.template.IProvider
""".trimIndent()
