package io.github.chenfei0928.lang

import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

fun Any.toString0(
    vararg field: KProperty0<*>,
) = buildString {
    append(this@toString0.javaClass.simpleName)
    append('(')
    field.forEachIndexed { index, kProperty0 ->
        if (index != 0) {
            append(", ")
        }
        append(kProperty0.name)
        append('=')
        append(kProperty0.get())
    }
    append(')')
}

fun <T : Any> T.toString1(
    vararg field: KProperty1<T, *>,
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
    field: Array<T>,
): String where T : KCallable<*>, T : () -> Any = buildString {
    append(this@toStringT.javaClass.simpleName)
    append('(')
    field.forEachIndexed { index, kProperty0 ->
        if (index != 0) {
            append(", ")
        }
        append(kProperty0.name)
        append('=')
        append(kProperty0())
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

fun Any.toStringRef(fields: Array<Any>) = buildString {
    append(this@toStringRef.javaClass.simpleName)
    append('(')
    fields.forEachIndexed { index, field ->
        if (index != 0) {
            append(", ")
        }
        when (field) {
            is Pair<*, *> -> {
                val (key, value) = field
                append(key)
                append('=')
                append(getValue(this@toStringRef, value))
            }
            is KCallable<*> -> {
                append(field.name)
                append('=')
                append(getValue(this@toStringRef, field))
            }
            else -> {
                append(field.toString())
            }
        }
    }
    append(')')
}

private fun getValue(
    thisRef: Any, field: Any?,
): Any? = when (field) {
    is KProperty<*> -> when (field) {
        is KProperty0<*> ->
            field.get()
        is KProperty1<*, *> ->
            (field as KProperty1<Any, *>).get(thisRef)
        else -> field
    }
    is KFunction<*> -> when (field) {
        is kotlin.jvm.functions.Function0<*> ->
            field()
        is kotlin.jvm.functions.Function1<*, *> ->
            (field as Any.() -> Any)(thisRef)
        else -> field
    }
    else -> field
}
