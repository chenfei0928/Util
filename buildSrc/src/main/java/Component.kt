import com.android.build.gradle.internal.plugins.AppPlugin
import com.android.build.gradle.internal.plugins.LibraryPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-30 17:38
 */
fun Project.applyComponent() {
    if (plugins.hasPlugin(AppPlugin::class.java)) {
        applyApp()
    } else if (plugins.hasPlugin(LibraryPlugin::class.java)) {
        applyLib()
    }
    applyVersion()

    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        defaultConfig {
            minSdk = 21
        }

        /**
         * 编译类型
         * {@link com.android.build.gradle.internal.dsl.BuildType}
         */
        buildTypes {
            // release正式包
            maybeCreate("release").apply {
                postprocessing {
                    // 混淆文件
                    proguardFile("proguard-rules.pro")
                }
            }
        }

        buildFeatures.run {
            viewBinding = true
        }

        aaptOptions {
            // 禁用cruncher, 以加速编译
            cruncherEnabled = false
            cruncherProcesses = 0
        }

        compileOptions {
            // 开启增量编译
            incremental = true
        }
    }

    // DI依赖注入、Debug调试工具、运行时权限处理依赖
    dependencies {
        implementation(project(":thirdExt:lib_LibraryHelper"))
        implementation(project(":lib:lib_util"))
        implementation(project(":lib:lib_base"))

        implementation(DepsAndroidx.multidex.core)
    }
}
