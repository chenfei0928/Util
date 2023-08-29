package io.github.chenfei0928.android

import io.github.chenfei0928.Contract
import io.github.chenfei0928.util.*
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * 通用配置，用于任何Android的Library或Application
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-11-10 14:57
 */
fun Project.applyCommon(appendBuildConfig: Boolean = true) {
    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        compileSdkVersion(Contract.compileSdkVersion)

        defaultConfig {
            proguardFile("proguard-rules.pro")
            // 添加文件夹下的混淆配置文件
            fileTree(Contract.PROGUARD_FILES_DIR).forEach { file ->
                if (file.path.endsWith(".pro")) {
                    proguardFile(file)
                }
            }

            consumerProguardFile("consumer-rules.pro")

            // 添加文件夹下的混淆配置文件
            fileTree(Contract.PROGUARD_FILES_DIR) {
                include("*.pro")
            }.forEach { file ->
                consumerProguardFile(file)
            }
        }

        buildTypes {
            fun com.android.build.api.dsl.BuildType.buildConfigFields(
                thirdSdkEnable: Boolean, loggable: Boolean, toastFullNetApiErrorResp: Boolean
            ) {
                if (!appendBuildConfig) {
                    return
                }
                buildConfigField("boolean", "thirdSdkEnable", "$thirdSdkEnable")
                buildConfigField("boolean", "loggable", "$loggable")
                buildConfigField("boolean", "toastFullNetApiErrorResp", "$toastFullNetApiErrorResp")
            }
            release {
                buildConfigFields(
                    thirdSdkEnable = true,
                    loggable = false,
                    toastFullNetApiErrorResp = false,
                )
            }
            prerelease {
                isDebuggable = true

                buildConfigFields(
                    thirdSdkEnable = true,
                    loggable = true,
                    toastFullNetApiErrorResp = true,
                )
            }
            qatest {
                isDebuggable = true

                buildConfigFields(
                    thirdSdkEnable = true,
                    loggable = true,
                    toastFullNetApiErrorResp = true,
                )
            }
            debug {
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
            cruncherEnabled = io.github.chenfei0928.Env.containsReleaseBuild
            cruncherProcesses = 0
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
            // 开启增量编译
            incremental = true
        }
    }

    dependencies {
        implementation(fileTree("libs") {
            include("*.jar", "*.aar", "*.so")
        })
    }
}
