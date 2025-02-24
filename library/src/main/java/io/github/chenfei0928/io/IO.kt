package io.github.chenfei0928.io

import android.os.Build
import java.io.File
import java.io.InputStream
import java.io.Reader

/**
 * Kotlin语言库的[Reader.forEachLine]不是inline行内优化，使用此方法替换
 * [kotlin.io.forEachLine]
 */
inline fun Reader.forEachLine(block: (String) -> Unit) {
    buffered().use {
        it.lineSequence().forEach(block)
    }
}

/**
 * Kotlin语言库的[File.forEachLine]不是inline行内优化，使用此方法替换
 * [kotlin.io.forEachLine]
 */
inline fun File.forEachLine(block: (String) -> Unit) {
    reader().forEachLine(block)
}

fun InputStream.readNBytesCompat(
    size: Int
): ByteArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    readNBytes(size)
} else {
    val bytes = ByteArray(size)
    var n = 0
    while (n < size) {
        val count = read(bytes, n, size - n)
        if (count < 0)
            break
        n += count
    }
    read(bytes, 0, size)
    bytes
}
