package io.github.chenfei0928.android

import com.tencent.shadow.core.gradle.ShadowPlugin
import io.github.chenfei0928.util.implementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

private val shadowVersion = "local-dc5bafb2-SNAPSHOT"

/**
 * @author chenf()
 * @date 2025-04-08 15:34
 */
fun Project.applyShadowHost() {
    dependencies {
        implementation("com.tencent.shadow.core:common:$shadowVersion")
        implementation("com.tencent.shadow.dynamic:dynamic-host:$shadowVersion")
    }
}

fun Project.applyShadowPlugin() {
    apply<ShadowPlugin>()

    dependencies {
        implementation("com.tencent.shadow.core:runtime:$shadowVersion")
    }
}
