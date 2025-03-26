package io.github.chenfei0928.lang

import androidx.collection.ArrayMap
import androidx.collection.LruCache
import io.github.chenfei0928.preference.base.FieldAccessor
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
            Modifier.STATIC !in it.modifiers
        }.onEach {
            it.isAccessible = true
        }
    }
}

fun Any?.toStringByReflect(): String {
    return if (this == null) {
        "null"
    } else if (toStringWasOverrideCache.getOrPut(javaClass) {
            javaClass.getMethod("toString").declaringClass != Any::class.java
        }) {
        // 如果该类的 toString 方法被重写过（包裹其父类）直接调用toString方法输出
        this.toString()
    } else when (this) {
        is Array<*> -> joinToString(
            prefix = "[",
            postfix = "]",
            separator = ", ",
            transform = { toStringByReflect() }
        )
        is ByteArray -> contentToString()
        is ShortArray -> contentToString()
        is IntArray -> contentToString()
        is LongArray -> contentToString()
        is CharArray -> contentToString()
        is FloatArray -> contentToString()
        is DoubleArray -> contentToString()
        is BooleanArray -> contentToString()
        else -> toStringByReflectImpl()
    }
}

private fun Any.toStringByReflectImpl() = buildString {
    // 不是数组，toString 也没有被重写过，调用反射输出每一个字段
    var thisClass: Class<*> = this@toStringByReflectImpl.javaClass
    append(thisClass.simpleName)
    append('(')
    var hasAnyField = false
    while (thisClass != Any::javaClass) {
        val fields = classFieldsCache[thisClass]!!
        fields.forEach { field ->
            hasAnyField = true
            append(field.name)
            append('=')
            val value = field[this@toStringByReflectImpl]
            if (value == this@toStringByReflectImpl) {
                append("this")
            } else {
                append(value?.toStringByReflect())
            }
            append(',')
        }
        thisClass = thisClass.getSuperclass()
    }
//    if (this.charAt(length - 1) == ',') {
    if (hasAnyField) {
        replace(length - 1, length, ")")
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
                append(getValue(this@toStringAny, value).toStringByReflect())
            }
            is KCallable<*> -> {
                append(field.name)
                append('=')
                append(getValue(this@toStringAny, field).toStringByReflect())
            }
            else -> {
                append(field.toStringByReflect())
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
