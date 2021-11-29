import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * 百度统计
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-31 17:32
 */
fun Project.applyBaiduMtj(appKey: String) {
    //apply plugin: 'mtj-circle-plugin'

    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        defaultConfig {
            manifestPlaceholders["BAIDU_STAT_ID"] = appKey
        }
    }

//    extensions.configure<PluginConfig>(
//        "MtjCirclePluginConfig"
//    ) {
//        // 设置appkey，必须设置，否则插件使用无效
//        appkey = appKey
//        // 设置debug 开关，默认关闭，如果需要查看日志则打开开关（true），建议正式版关闭以避免影响性能
//        debug = !Env.isRelease
//        // 默认启动此插件，如果开发者不需要可以禁用（false）
//        enabled = Env.isRelease
//    }

    dependencies {
        add("compileOnly", Deps.analytics.baidu.mtj)
    }
}
