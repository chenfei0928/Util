package io.github.chenfei0928.compiler

import io.github.chenfei0928.util.child
import io.github.chenfei0928.util.tmpProguardFilesDir
import org.gradle.api.Project
import java.io.File

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-08-22 10:21
 */
fun Project.writeTmpProguardFile(fileName: String, content: String): File {
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
