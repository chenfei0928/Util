import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
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

    val hasKapt = plugins.hasPlugin("kotlin-kapt")
    if (hasKapt) {
        (this as org.gradle.api.plugins.ExtensionAware).extensions.configure<org.jetbrains.kotlin.gradle.plugin.KaptExtension>(
            "kapt"
        ) {
            arguments {
                arg("AROUTER_MODULE_NAME", project.name)
            }
        }
    }

    dependencies {
        implementation(Deps.lib.aRouter.api)
        annotationProcessor(Deps.lib.aRouter.compiler)
        if (hasKapt) {
            kapt(Deps.lib.aRouter.compiler)
        }
    }
}
