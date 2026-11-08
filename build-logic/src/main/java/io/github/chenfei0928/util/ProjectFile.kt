package io.github.chenfei0928.util

import io.github.chenfei0928.Contract
import org.gradle.api.Project
import java.io.File

/**
 * @author chenfei()
 * @date 2022-07-06 13:52
 */
internal val Project.mappingFileSaveDir: File
    get() = projectDir.child { Contract.mappingFileSaveDirName }

internal val Project.buildOutputsDir: File
    get() = layout.buildDirectory.child { Contract.outputs }

internal val Project.tmpProguardFilesDir: File
    get() = layout.buildDirectory.child { "tmp" / Contract.PROGUARD_FILES_DIR }

internal fun Project.writeTmpProguardFile(fileName: String, content: String): File {
    return tmpProguardFilesDir.child {
        fileName
    }.also {
        if (!it.exists() || it.readText() != content) {
            it.parentFile.mkdirs()
            it.delete()
            it.createNewFile()
            it.writeText(content)
        }
    }
}
