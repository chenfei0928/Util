package io.github.chenfei0928.android

import com.didi.drouter.RouterPluginKt
import com.didi.drouter.plugin.RouterSetting
import io.github.chenfei0928.Env
import io.github.chenfei0928.util.implementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

/**
 * 滴滴的路由框架
 *
 * [DRouter](https://github.com/didi/DRouter#%E4%BD%BF%E7%94%A8%E6%96%87%E6%A1%A3)
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2023-02-23 15:47
 */
fun Project.applyDRouter() {
    if (plugins.hasPlugin(com.android.build.gradle.AppPlugin::class.java)) {
        apply<RouterPluginKt>()

        extensions.configure<RouterSetting>("drouter") {
            debug = !Env.containsReleaseBuild
        }
    }

    dependencies {
        // Router、Service
        implementation("io.github.didi:drouter-api:2.4.6")
        // Page
        implementation("io.github.didi:drouter-api-page:1.0.0")
        // Process
        implementation("io.github.didi:drouter-api-process:1.0.0")
    }
}
