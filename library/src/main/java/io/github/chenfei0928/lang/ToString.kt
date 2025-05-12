package io.github.chenfei0928.lang

import androidx.collection.ArrayMap
import androidx.collection.LruCache
import io.github.chenfei0928.preference.base.FieldAccessor
import io.github.chenfei0928.reflect.isWriteByKotlin
import java.lang.ref.Reference
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

private val toStringWasOverrideCache: MutableMap<Class<*>, Boolean> = ArrayMap()
private val classNonStaticFieldsCache = object : LruCache<Class<*>, List<Field>>(32) {
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

fun Any?.toStringByReflect(): String = StringBuilder().appendByReflect(this).toString()

fun StringBuilder.appendByReflect(any: Any?): StringBuilder = when (any) {
    null -> append("null")
    is Class<*> -> {
        append(any.name)
        append('{')
        // java，打印当前类自身定义的static字段
        any.declaredFields.filter {
            Modifier.isStatic(it.modifiers) && !it.isSynthetic
        }.forEachIndexed { i, field ->
            if (i != 0) {
                append(", ")
            }
            field.isAccessible = true
            append(field.name)
            append('=')
            append(field.get(null))
        }
        append('}')
        this
    }
    is KClass<*> -> {
        append(any.qualifiedName)
        // kotlin，打印伴生对象的字段
        val companionKls = if (any.isCompanion)
            any else any.companionObject
        if (companionKls != null) {
            appendKotlinComponentMembersByKtReflect(companionKls)
        } else {
            append("{}")
        }
    }
    is Array<*> -> {
        append('[')
        any.forEachIndexed { i, element ->
            if (i != 0) {
                append(", ")
            }
            appendByReflect(element)
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
        any.forEachIndexed { i, element ->
            if (i != 0) {
                append(", ")
            }
            appendByReflect(element)
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
    } else appendObjectByReflectImpl(any)
}

private fun <T : Any> StringBuilder.appendKotlinComponentMembersByKtReflect(
    companionKls: KClass<T>, companionObj: T = companionKls.objectInstance!!
): StringBuilder = apply {
    append('{')
    // kotlin，打印伴生对象自身定义的字段
    companionKls.declaredMemberProperties.forEachIndexed { i, property ->
        if (i != 0) {
            append(", ")
        }
        property.isAccessible = true
        append(property.name)
        append('=')
        append(property.get(companionObj))
    }
    append('}')
}

private fun StringBuilder.appendObjectByReflectImpl(any: Any) = apply {
    if (any.javaClass.isWriteByKotlin) {
        // 如果当前实例的类是kotlin类，且当前对象是伴生对象，尝试打印伴生对象的字段
        val kClass = any.javaClass.kotlin
        if (kClass.isCompanion) {
            append(kClass.qualifiedName)
            appendKotlinComponentMembersByKtReflect(kClass)
            return@apply
        }
    }

    // 不是数组，toString 也没有被重写过，调用反射输出每一个字段
    var thisClass: Class<*>? = any.javaClass
    append(thisClass?.simpleName)
    append('(')
    var hasAnyField = false
    while (thisClass != null && thisClass != Any::class.java) {
        // 打印当前类的非static字段
        val fields = classNonStaticFieldsCache[thisClass]!!
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
