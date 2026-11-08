package io.github.chenfei0928.util

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider

internal fun Project.libsForCatalog(): VersionCatalog {
    val catalogs = extensions.getByType(VersionCatalogsExtension::class.java)
    return catalogs.named("libs")
}

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { IllegalStateException("Library '$alias' not found in version catalog") }

internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias).orElseThrow { IllegalStateException("Version '$alias' not found in version catalog") }.requiredVersion