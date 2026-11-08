package io.github.chenfei0928.compiler

import io.github.chenfei0928.Env
import io.github.chenfei0928.util.checkApp
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import top.niunaijun.blackobfuscator.BlackObfuscatorExtension
import top.niunaijun.blackobfuscator.ObfPlugin

/**
 * DEX控制流混淆，但会使包大小暴增
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-02-17 13:39
 */
fun Project.applyAppBlackObfuscator() {
    checkApp("applyBlackObfuscator")

    apply<ObfPlugin>()

    // 加入混淆配置
    extensions.configure<BlackObfuscatorExtension>("BlackObfuscator") {
        // 是否启用
        enabled = Env.containsReleaseBuild
        // 混淆深度
        depth = 2
        // 需要混淆的包或者类(匹配前面一段)
        setObfClass()
        // blackClass中的包或者类不会进行混淆(匹配前面一段)
        setBlackClass()
    }
}
