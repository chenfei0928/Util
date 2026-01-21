package io.github.chenfei0928.android

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.internal.plugins.AppPlugin
import com.android.build.gradle.internal.plugins.LibraryPlugin
import io.github.chenfei0928.DepsAndroidx
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.implementation
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
        applyLibrary()
    }

    buildSrcAndroid<CommonExtension>().apply {
        buildFeatures.run {
            viewBinding = true
        }
    }

    // DI依赖注入、Debug调试工具、运行时权限处理依赖
    dependencies {
        implementation(DepsAndroidx.multidex.core)
    }
}
