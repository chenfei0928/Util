package io.github.chenfei0928.lang

import io.github.chenfei0928.io.AppendableWriter
import java.io.PrintWriter

/**
 * @author chenf()
 * @date 2026-01-05 14:00
 */
fun Throwable.printStackTrace(appendable: Appendable) {
    printStackTrace(PrintWriter(AppendableWriter(appendable)))
}

fun Throwable.getStackTraceString(): String = StringBuilder().also {
    printStackTrace(it)
}.toString()
