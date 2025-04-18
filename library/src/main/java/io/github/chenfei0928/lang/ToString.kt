package io.github.chenfei0928.lang

import androidx.collection.ArrayMap
import androidx.collection.LruCache
import io.github.chenfei0928.preference.base.FieldAccessor
import java.lang.ref.Reference
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

private val toStringWasOverrideCache: MutableMap<Class<*>, Boolean> = ArrayMap()
private val classFieldsCache = object : LruCache<Class<*>, List<Field>>(32) {
    override fun create(key: Class<*>): List<Field>? {
        return key.declaredFields.filter {
            Modifier.TRANSIENT !in it.modifiers
                    && Modifier.STATIC !in it.modifiers
                    && !it.isSynthetic
        }.onEach {
            it.isAccessible = true
        }
    }
}

fun Any?.toStringByReflect() = StringBuilder().appendByReflect(this)

fun StringBuilder.appendByReflect(any: Any?): StringBuilder = when (any) {
    null -> append("null")
    is Array<*> -> {
        append('[')
        any.forEachIndexed { i, it ->
            if (i != 0) {
                append(", ")
            }
            appendByReflect(it)
        }
        append(']')
    }
    is ByteArray -> append(any.contentToString())
    is ShortArray -> append(any.contentToString())
    is IntArray -> append(any.contentToString())
    is LongArray -> append(any.contentToString())
    is CharArray -> append(any.contentToString())
    is FloatArray -> append(any.contentToString())
    is DoubleArray -> append(any.contentToString())
    is BooleanArray -> append(any.contentToString())
    is CharSequence -> append(any)
    is Iterable<*> -> {
        append('[')
        any.forEachIndexed { i, it ->
            if (i != 0) {
                append(", ")
            }
            appendByReflect(it)
        }
        append(']')
    }
    is Map<*, *> -> if (isEmpty()) {
        append(any.javaClass.simpleName)
        append("(empty)")
    } else {
        append('[')
        any.forEach {
            appendByReflect(it.key)
            append('=')
            appendByReflect(it.value)
            append(", ")
        }
        replace(length - 2, length, "]")
    }
    is Reference<*> -> appendByReflect(any.get())
    else -> if (toStringWasOverrideCache.getOrPut(any.javaClass) {
            any.javaClass.getMethod("toString").declaringClass != Any::class.java
        }) {
        // 如果该类的 toString 方法被重写过（包裹其父类）直接调用toString方法输出
        append(any.toString())
    } else appendByReflectImpl(any)
}

private fun StringBuilder.appendByReflectImpl(any: Any) = apply {
    // 不是数组，toString 也没有被重写过，调用反射输出每一个字段
    var thisClass: Class<*>? = any.javaClass
    append(thisClass?.simpleName)
    append('(')
    var hasAnyField = false
    while (thisClass != null && thisClass != Any::javaClass) {
        val fields = classFieldsCache[thisClass]!!
        fields.forEach { field ->
            hasAnyField = true
            append(field.name)
            append('=')
            val value = field[any]
            if (value == any) {
                append("this")
            } else {
                appendByReflect(value)
            }
            append(", ")
        }
        thisClass = thisClass.getSuperclass()
    }
//    if (this.charAt(length - 1) == ',') {
    if (hasAnyField) {
        replace(length - 2, length, ")")
    } else {
        append(')')
    }
}

fun Any.toStringAny(vararg fields: Any) = buildString {
    append(this@toStringAny.javaClass.simpleName)
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
                appendByReflect(getValue(this@toStringAny, value))
            }
            is KCallable<*> -> {
                append(field.name)
                append('=')
                appendByReflect(getValue(this@toStringAny, field))
            }
            else -> {
                appendByReflect(field)
            }
        }
    }
    append(')')
}

@Suppress("UNCHECKED_CAST")
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
    is FieldAccessor.Field<*, *> -> {
        (field as FieldAccessor.Field<Any, *>).get(thisRef)
    }
    else -> field
}
