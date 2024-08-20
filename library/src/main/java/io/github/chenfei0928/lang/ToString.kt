package io.github.chenfei0928.lang

import kotlin.reflect.KCallable
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

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
