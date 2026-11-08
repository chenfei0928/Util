package io.github.chenfei0928.compiler

import io.github.chenfei0928.Env
import io.michaelrocks.paranoid.plugin.ParanoidExtension
import io.michaelrocks.paranoid.plugin.ParanoidPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

/**
 * Paranoid字符串混淆，可以拿来加密在Java/Kotlin中的AppKey之类字符串
 *
 * [Paranoid](https://github.com/MichaelRocks/paranoid)
 */
fun Project.applyParanoid() {
    apply<ParanoidPlugin>()

    extensions.configure<ParanoidExtension>("paranoid") {
        isEnabled = Env.containsReleaseBuild
    }
}
