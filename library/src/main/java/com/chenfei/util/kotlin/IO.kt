package com.chenfei.util.kotlin

import java.io.File
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
