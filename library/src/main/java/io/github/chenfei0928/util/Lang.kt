package io.github.chenfei0928.util

import java.util.Arrays

operator fun Int.contains(other: Int): Boolean {
    return other and this == other
}

operator fun Long.contains(other: Long): Boolean {
    return other and this == other
}

infix fun Int.anyIn(other: Int): Boolean {
    return other and this != 0
}

/**
 * [Arrays.deepEquals]
 */
fun Any?.deepEquals(b: Any?): Boolean {
    return when {
        this === b -> true
        this == null || b == null -> false
        this is Array<*> && b is Array<*> -> this.contentDeepEquals(b)
        this is ByteArray && b is ByteArray -> this.contentEquals(b)
        this is ShortArray && b is ShortArray -> this.contentEquals(b)
        this is IntArray && b is IntArray -> this.contentEquals(b)
        this is LongArray && b is LongArray -> this.contentEquals(b)
        this is CharArray && b is CharArray -> this.contentEquals(b)
        this is FloatArray && b is FloatArray -> this.contentEquals(b)
        this is DoubleArray && b is DoubleArray -> this.contentEquals(b)
        this is BooleanArray && b is BooleanArray -> this.contentEquals(b)
        else -> this == b
    }
}
