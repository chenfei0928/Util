package io.github.chenfei0928.android

import io.github.chenfei0928.Contract
import io.github.chenfei0928.util.buildSrcAndroid
import org.gradle.api.Project

/**
 * 通用配置，用于任何Android的Library
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-10-28 18:47
 */
fun Project.applyLibrary(appendBuildConfig: Boolean = true) {
    applyCommon(appendBuildConfig = appendBuildConfig)

    buildSrcAndroid<com.android.build.gradle.LibraryExtension>().apply {
        defaultConfig {
            minSdk = Contract.minSdkVersion
        }

        lint {
            abortOnError = false
        }
    }
}
