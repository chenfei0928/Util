package io.github.chenfei0928.compiler

import io.github.chenfei0928.Env
import io.github.chenfei0928.util.checkApp
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import plugin.RocketXPlugin
import plugin.bean.RocketXBean

/**
 * [RocketXPlugin](https://github.com/trycatchx/RocketXPlugin)
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-02-22 18:28
 */
fun Project.applyAppRocketX() {
    checkApp("applyAppRocketX")

    if (Env.containsReleaseBuild) {
        return
    }

    apply<RocketXPlugin>()

    extensions.configure<RocketXBean>("RocketX") {
        //指定哪些模块不打成 aar ，字符串为 module.path
        excludeModule = setOf()
    }
}
