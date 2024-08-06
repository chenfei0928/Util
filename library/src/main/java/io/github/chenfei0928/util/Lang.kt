package io.github.chenfei0928.util

import java.util.Arrays
import kotlin.reflect.KCallable
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

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

fun Any.toString0(
    vararg field: KProperty0<*>
) = buildString {
    append(this@toString0.javaClass.simpleName)
    append('(')
    field.forEachIndexed { index, kProperty1 ->
        if (index != 0) {
            append(", ")
        }
        append(kProperty1.name)
        append('=')
        append(kProperty1.get())
    }
    append(')')
}

fun <T : Any> T.toString1(
    vararg field: KProperty1<T, *>
) = buildString {
    append(this@toString1.javaClass.simpleName)
    append('(')
    field.forEachIndexed { index, kProperty1 ->
        if (index != 0) {
            append(", ")
        }
        append(kProperty1.name)
        append('=')
        append(kProperty1.get(this@toString1))
    }
    append(')')
}

fun <T> Any.toStringT(
    field: Array<T>
): String where T : KCallable<*>, T : () -> Any = buildString {
    append(this@toStringT.javaClass.simpleName)
    append('(')
    field.forEachIndexed { index, kProperty1 ->
        if (index != 0) {
            append(", ")
        }
        append(kProperty1.name)
        append('=')
        append(kProperty1())
    }
    append(')')
}

fun Any.toStringKV(vararg field: Pair<String, Any?>) = buildString {
    append(this@toStringKV.javaClass.simpleName)
    append('(')
    field.forEachIndexed { index, (key, value) ->
        if (index != 0) {
            append(", ")
        }
        append(key)
        append('=')
        append(value)
    }
    append(')')
}
