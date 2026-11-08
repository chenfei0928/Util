package io.github.chenfei0928.compiler

import io.github.chenfei0928.util.compileOnly
import net.ltgt.gradle.errorprone.ErrorProneOptions
import net.ltgt.gradle.errorprone.ErrorPronePlugin
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * https://github.com/google/error-prone
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-07-05 16:23
 */
fun Project.applyErrorProne() {
    apply<ErrorPronePlugin>()

    extensions.configure<ErrorProneOptions>("errorprone") {
    }

    tasks.withType<JavaCompile>().configureEach {
        options.errorprone.run {
//            excludedPaths.set(this@applyErrorProne.buildDir.path.replace("\\", "\\\\"))
            isEnabled.set(true)
            disableWarningsInGeneratedCode.set(true)
        }
    }

    dependencies {
        add("errorprone", "com.google.errorprone:error_prone_core:2.28.0")
        compileOnly("com.google.errorprone:error_prone_core:2.28.0")
    }
}
