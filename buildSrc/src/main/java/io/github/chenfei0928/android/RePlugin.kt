package io.github.chenfei0928.android

import com.qihoo360.replugin.gradle.host.Replugin
import com.qihoo360.replugin.gradle.host.RepluginConfig
import com.qihoo360.replugin.gradle.plugin.ReClassConfig
import com.qihoo360.replugin.gradle.plugin.ReClassPlugin
import io.github.chenfei0928.Deps
import io.github.chenfei0928.util.implementation
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

/**
 * 应用 RePlugin 的 Plugin 工程配置
 *
 * 修改了 Replugin plugin 的源代码
 * - 将 [PluginDebugger#L57](https://github.com/Qihoo360/RePlugin/blob/36db020/replugin-plugin-gradle/src/main/groovy/com/qihoo360/replugin/gradle/plugin/debugger/PluginDebugger.groovy#L57)
 * 修改为 `adbFile = project.extensions.getByName('android').adbExecutable` ，
 * - 将 [ReClassPlugin#L145](https://github.com/Qihoo360/RePlugin/blob/36db020/replugin-plugin-gradle/src/main/groovy/com/qihoo360/replugin/gradle/plugin/ReClassPlugin.groovy#L145)
 * 注释掉
 * - 将 [ProviderExprEditor2#L29](https://github.com/Qihoo360/RePlugin/blob/36db020/replugin-plugin-gradle/src/main/groovy/com/qihoo360/replugin/gradle/plugin/injector/provider/ProviderExprEditor2.groovy#L29)
 * 修改为 `static def PROVIDER_CLASS = 'com.qihoo360.replugin.component.provider.PluginProviderClient2'`
 *
 * 将 `JAVA_HOME` 设置为 JDK 17 ，cd 到工程目录下，调用 `gradlew publishMavenJavaPublicationToMavenLocal`
 * 将该工程打包到 `~/.m2` 目录下，并将其jar文件复制到 libs 目录下使用
 *
 * @param name
 */
fun Project.applyRePluginPlugin(name: String = project.name) {
    apply<ReClassPlugin>()

    extensions.configure<ReClassConfig>("repluginPluginConfig") {
        appModule = "app"
        //插件名
        pluginName = name
        //宿主app的包名
        hostApplicationId = ""
        //宿主app的启动activity
        hostAppLauncherActivity = ""
    }

    dependencies {
        implementation(Deps.framework.repluginPlugin)
    }
}
