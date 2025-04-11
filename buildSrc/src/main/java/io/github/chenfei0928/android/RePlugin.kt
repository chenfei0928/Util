package io.github.chenfei0928.android

import io.github.chenfei0928.Deps
import io.github.chenfei0928.util.implementation
import com.qihoo360.replugin.gradle.host.Replugin
import com.qihoo360.replugin.gradle.host.RepluginConfig
import com.qihoo360.replugin.gradle.plugin.ReClassConfig
import com.qihoo360.replugin.gradle.plugin.ReClassPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

/**
 * @author chenf()
 * @date 2025-03-17 17:41
 */
fun Project.applyRePluginHost() {
    apply<Replugin>()

    extensions.configure<RepluginConfig>("repluginHostConfig") {
        useAppCompat = true
        useAndroidX = true
    }

    dependencies {
        implementation(Deps.framework.repluginHost)
    }
}

fun Project.applyRePluginPlugin(name: String = project.name) {
    apply<ReClassPlugin>()

    extensions.configure<ReClassConfig>("repluginPluginConfig") {
        appModule = "app"
        //插件名
        pluginName = name
        //宿主app的包名
        hostApplicationId = "com.cocos.adventure.app"
        //宿主app的启动activity
        hostAppLauncherActivity = "com.xi.quickgame.ui.main.main.MainActivity"
    }

    dependencies {
        implementation(Deps.framework.repluginPlugin)
    }
}
