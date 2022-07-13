package io.github.chenfei0928.util

import java.io.File

/**
 * @author chenfei()
 * @date 2022-04-02 18:42
 */
internal inline class FilePathBuilder(
    private val parent: File
) {

    operator fun String.div(child: String): String {
        return this@div + File.separatorChar + child
    }

    inline fun apply(block: FilePathBuilder.() -> String): File {
        return File(parent, block(this))
    }
}

internal inline fun File.child(builder: FilePathBuilder.() -> String): File {
    return FilePathBuilder(this).apply(builder)
}
