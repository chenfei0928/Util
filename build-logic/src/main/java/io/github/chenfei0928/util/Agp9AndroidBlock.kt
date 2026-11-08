package io.github.chenfei0928.util

import com.android.build.api.dsl.AndroidResources
import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.BuildFeatures
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.CompileOptions
import com.android.build.api.dsl.DefaultConfig
import com.android.build.api.dsl.DynamicFeatureExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Packaging
import com.android.build.api.dsl.TestExtension
import com.android.build.api.dsl.TestOptions
import io.github.chenfei0928.Env
import org.gradle.api.NamedDomainObjectContainer
import java.io.File

/**
 * @author chenf()
 * @date 2026-01-21 15:07
 */
internal fun CommonExtension.defaultConfig(
    block: DefaultConfig.() -> Unit
) = when (this) {
    is ApplicationExtension -> defaultConfig(block)
    is LibraryExtension -> defaultConfig(block)
    is DynamicFeatureExtension -> defaultConfig(block)
    is TestExtension -> defaultConfig(block)
    else -> throwException()
}

internal fun CommonExtension.buildTypes(
    block: NamedDomainObjectContainer<out BuildType>.() -> Unit
) = when (this) {
    is ApplicationExtension -> buildTypes(block)
    is LibraryExtension -> buildTypes(block)
    is DynamicFeatureExtension -> buildTypes(block)
    is TestExtension -> buildTypes(block)
    else -> throwException()
}

internal fun CommonExtension.sourceSets(
    block: NamedDomainObjectContainer<out AndroidSourceSet>.() -> Unit
) = when (this) {
    is ApplicationExtension -> sourceSets(block)
    is LibraryExtension -> sourceSets(block)
    is DynamicFeatureExtension -> sourceSets(block)
    is TestExtension -> sourceSets(block)
    else -> throwException()
}

internal fun CommonExtension.androidResources(
    block: AndroidResources.() -> Unit
) = when (this) {
    is ApplicationExtension -> androidResources(block)
    is LibraryExtension -> androidResources(block)
    is DynamicFeatureExtension -> androidResources(block)
    is TestExtension -> androidResources(block)
    else -> throwException()
}

internal fun CommonExtension.compileOptions(
    block: CompileOptions.() -> Unit
) = when (this) {
    is ApplicationExtension -> compileOptions(block)
    is LibraryExtension -> compileOptions(block)
    is DynamicFeatureExtension -> compileOptions(block)
    is TestExtension -> compileOptions(block)
    else -> throwException()
}

internal fun CommonExtension.buildFeatures(
    block: BuildFeatures.() -> Unit
) = when (this) {
    is ApplicationExtension -> buildFeatures(block)
    is LibraryExtension -> buildFeatures(block)
    is DynamicFeatureExtension -> buildFeatures(block)
    is TestExtension -> buildFeatures(block)
    else -> throwException()
}

internal fun CommonExtension.packaging(
    block: Packaging.() -> Unit
) = when (this) {
    is ApplicationExtension -> packaging(block)
    is LibraryExtension -> packaging(block)
    is DynamicFeatureExtension -> packaging(block)
    is TestExtension -> packaging(block)
    else -> throwException()
}

internal fun CommonExtension.testOptions(
    block: TestOptions.() -> Unit
) = when (this) {
    is ApplicationExtension -> testOptions(block)
    is LibraryExtension -> testOptions(block)
    is DynamicFeatureExtension -> testOptions(block)
    is TestExtension -> testOptions(block)
    else -> throwException()
}

val ApplicationExtension.buildToolsDir: File
    get() = File(
        File(Env.gradle.rootProject.properties["sdk.dir"] as String, "build-tools"),
        buildToolsVersion
    )

val ApplicationExtension.adbExecutable: File
    get() = if (Env.isWindows) {
        File(
            Env.gradle.rootProject.properties["sdk.dir"] as String,
            "platform-tools\\adb.exe"
        )
    } else {
        File(
            Env.gradle.rootProject.properties["sdk.dir"] as String,
            "platform-tools/adb"
        )
    }

private fun CommonExtension.throwException(): Nothing =
    throw IllegalArgumentException("Unknown extension $this")
