import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-27 18:28
 */
internal fun Project.applyCommon() {
    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        compileSdkVersion(Env.compileSdkVersion)

        defaultConfig {
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            consumerProguardFile("consumer-rules.pro")

            // 添加文件夹下的混淆配置文件
            fileTree("consumer-rules") { include("*.pro") }.forEach { file ->
                consumerProguardFile(file)
            }
        }

        ndkVersion = "22.0.7026061"

        buildTypes {
            fun com.android.build.api.dsl.BuildType.buildConfigFields(
                thirdSdkEnable: Boolean, loggable: Boolean, toastFullNetApiErrorResp: Boolean
            ) {
                buildConfigField("boolean", "thirdSdkEnable", "$thirdSdkEnable")
                buildConfigField("boolean", "loggable", "$loggable")
                buildConfigField("boolean", "toastFullNetApiErrorResp", "$toastFullNetApiErrorResp")
            }
            getByName("release") {
                postprocessing {
                    proguardFile("proguard-rules.pro")
                }

                buildConfigFields(
                    thirdSdkEnable = true,
                    loggable = false,
                    toastFullNetApiErrorResp = false,
                )
            }
            maybeCreate("abtest").apply {
                isDebuggable = true

                buildConfigFields(
                    thirdSdkEnable = false,
                    loggable = true,
                    toastFullNetApiErrorResp = false,
                )
            }
            getByName("debug") {
                isDebuggable = true
                isCrunchPngs = false

                buildConfigFields(
                    thirdSdkEnable = false,
                    loggable = true,
                    toastFullNetApiErrorResp = true,
                )
            }
        }
        // Debug时禁用PNG优化
        aaptOptions {
            cruncherEnabled = Env.containsReleaseBuild
        }
        // 移除lint检查的error
        lintOptions {
            isAbortOnError = false
            isCheckReleaseBuilds = false
            disable("InvalidPackage")
        }
        compileOptions {
            sourceCompatibility(JavaVersion.VERSION_1_8)
            targetCompatibility(JavaVersion.VERSION_1_8)
        }
    }

    // 同步依赖版本
    // https://juejin.im/post/5d2dee0851882569755f5494
    val needMargeVersion = listOf(
        Deps.lib.gson, Deps.network.okhttp, Deps.network.logging
    )
    configurations.all {
        resolutionStrategy {
            eachDependency {
                val foundMargeVersion = needMargeVersion.find {
                    it.startsWith(requested.group + ":" + requested.name)
                }
                if (foundMargeVersion != null) {
                    useVersion(
                        foundMargeVersion
                            .split(":")
                            .last()
                    )
                    because("use lastest version")
                }
            }
        }
    }

    dependencies {
        implementation(fileTree("libs") { include("*.jar", "*.aar") })
    }

    applyKotlin()
}
