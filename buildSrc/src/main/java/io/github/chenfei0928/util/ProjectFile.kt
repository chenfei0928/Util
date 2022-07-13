package io.github.chenfei0928.util

import org.gradle.api.Project
import java.io.File

/**
 * @author chenfei()
 * @date 2022-07-06 13:52
 */
internal val Project.mappingFileSaveDir: File
    get() = projectDir.child { io.github.chenfei0928.Contract.mappingFileSaveDirName }

internal val Project.buildOutputsDir: File
    get() = buildDir.child { io.github.chenfei0928.Contract.outputs }

internal val Project.tmpProguardFilesDir: File
    get() = buildDir.child { "tmp" / io.github.chenfei0928.Contract.PROGUARD_FILES_DIR }
