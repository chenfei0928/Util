package io.github.chenfei0928.android

import com.didiglobal.booster.gradle.BoosterPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

/**
 * @author chenf()
 * @date 2025-05-19 17:51
 */
fun Project.applyDidiBoost() {
    apply<BoosterPlugin>()
}
