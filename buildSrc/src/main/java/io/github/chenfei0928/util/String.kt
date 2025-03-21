package io.github.chenfei0928.util

import java.util.Locale

/**
 * @author chenf()
 * @date 2025-03-20 18:48
 */
fun String.replaceFirstCharToUppercase() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
}

fun String.replaceFirstCharToLowercase() = replaceFirstChar {
    if (it.isLowerCase()) it.toString() else it.lowercase(Locale.ROOT)
}
