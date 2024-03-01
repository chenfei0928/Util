package io.github.chenfei0928.util

import org.gradle.api.file.DirectoryProperty
import java.io.File

/**
 * @author chenfei()
 * @date 2022-04-02 18:42
 */
internal class FilePathBuilder {

    operator fun String.div(child: String): String {
        return this@div + File.separatorChar + child
    }

    inline fun apply(parent: File, block: FilePathBuilder.() -> String): File {
        return File(parent, block(this))
    }

    inline fun apply(parent: DirectoryProperty, block: FilePathBuilder.() -> String): File {
        return parent.dir(block(this)).get().asFile
    }
}

internal inline fun File.child(builder: FilePathBuilder.() -> String): File {
    return FilePathBuilder().apply(this, builder)
}

internal inline fun DirectoryProperty.child(builder: FilePathBuilder.() -> String): File {
    return FilePathBuilder().apply(this, builder)
}
