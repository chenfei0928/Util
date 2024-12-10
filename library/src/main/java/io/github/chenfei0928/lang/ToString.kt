package io.github.chenfei0928.lang

import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

fun Any.toString0(
    vararg fields: KProperty0<*>,
) = buildString {
    append(this@toString0.javaClass.simpleName)
    append('(')
    fields.forEachIndexed { index, kProperty0 ->
        if (index != 0) {
            append(", ")
        }
        append(kProperty0.name)
        append('=')
        append(kProperty0.get().toString())
    }
    append(')')
}

fun <T : Any> T.toString1(
    vararg fields: KProperty1<T, *>,
) = buildString {
    append(this@toString1.javaClass.simpleName)
    append('(')
    fields.forEachIndexed { index, kProperty1 ->
        if (index != 0) {
            append(", ")
        }
        append(kProperty1.name)
        append('=')
        append(kProperty1.get(this@toString1).toString())
    }
    append(')')
}

fun <T> Any.toStringT(
    fields: Array<T>,
): String where T : KCallable<*>, T : () -> Any? = buildString {
    append(this@toStringT.javaClass.simpleName)
    append('(')
    fields.forEachIndexed { index, kProperty0 ->
        if (index != 0) {
            append(", ")
        }
        append(kProperty0.name)
        append('=')
        append(kProperty0().toString())
    }
    append(')')
}

fun Any.toStringKV(vararg fields: Pair<String, Any?>) = buildString {
    append(this@toStringKV.javaClass.simpleName)
    append('(')
    fields.forEachIndexed { index, (key, value) ->
        if (index != 0) {
            append(", ")
        }
        append(key)
        append('=')
        append(value.toString())
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
                append(getValue(this@toStringRef, value).toString())
            }
            is KCallable<*> -> {
                append(field.name)
                append('=')
                append(getValue(this@toStringRef, field).toString())
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
        is Function0<*> ->
            field()
        is Function1<*, *> ->
            (field as Any.() -> Any)(thisRef)
        else -> field
    }
    else -> field
}
