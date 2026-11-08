package io.github.chenfei0928.android

import com.android.build.api.dsl.ApplicationExtension
import io.github.chenfei0928.Contract
import io.github.chenfei0928.Env
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.prerelease
import io.github.chenfei0928.util.qatest
import org.gradle.api.Project
import java.io.File

/**
 * 通用配置，用于任何Android的Application
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-30 16:18
 */
fun Project.applyApp() {
    applyCommon()

    buildSrcAndroid<ApplicationExtension>().apply {
        signingConfigs {
            register("config") {
                storeFile =
                    File(project.rootDir, project.properties["RELEASE_STORE_FILE"].toString())
                storePassword = project.properties["RELEASE_STORE_PASSWORD"].toString()
                keyAlias = project.properties["RELEASE_KEY_ALIAS"].toString()
                keyPassword = project.properties["RELEASE_KEY_PASSWORD"].toString()
            }
            getByName("debug") {
                storeFile = File(File(project.rootDir, "gradle"), "debug.keystore")
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
        defaultConfig {
            minSdk = Contract.minSdkVersion
            targetSdk = Contract.targetSdkVersion
            compileSdk = Contract.compileSdkVersion

            versionCode = Env.vcsVersionCode

            multiDexEnabled = true
            // 使用矢量图
            vectorDrawables.useSupportLibrary = true
        }

        buildTypes {
            // release正式包
            release {
                // 签名
                signingConfig = signingConfigs.getByName("config")
                // 后处理，对编译完成后的资源、代码文件进行处理
//                postprocessing {
//                    // 启用自动过滤删除无用res资源文件，依赖于 isMinifyEnabled
//                    // 但不清楚keep混淆R文件是否会影响该效果
//                    isRemoveUnusedResources = false
//                    // 代码混淆
//                    isObfuscate = true
//                    isOptimizeCode = true
//                    isRemoveUnusedCode = true
//                }
            }
            // PreRelease灰度测试包（预上线）
            prerelease {
                // 在release基础上添加允许debug开关
                this.initWith(getByName("release"))

                isDebuggable = true
            }
            // qaTest质量测试包（Quality Assurance）
            qatest {
                // 在release基础上添加允许debug开关
                this.initWith(getByName("release"))

                isDebuggable = true
            }
            // debug开发测试运行、包
            debug {
                isDebuggable = true
                isCrunchPngs = false
                // 签名
                signingConfig = signingConfigs.getByName("debug")
            }
        }

        // 如果出现META-INF重复的问题 - 友盟社会化登录、分享
        // 打包时排除一些文件
        packaging {
            resources.excludes.add("META-INF/beans.xml")
            // support支持库
            resources.excludes.add("META-INF/*.version")
            resources.excludes.add("androidsupportmultidexversion.txt")
            // Anko
            resources.excludes.add("META-INF/*.kotlin_module")
            // 小米推送的额外文件
            resources.excludes.add("miui_push_version")
            resources.excludes.add("push_version")
            // 部分sdk的混淆文件
            resources.excludes.add("META-INF/proguard/**")
        }

        // Debug时禁用Multi APK
        if (!Env.containsReleaseBuild) {
            splits.abi.isEnable = false
        }

        // Debug时不编译不必要的资源
        if (!Env.containsReleaseBuild) {
            productFlavors.all {
                androidResources.localeFilters.addAll(
                    arrayOf("zh", "zh-rCN")
                )
            }
        }
    }
}
