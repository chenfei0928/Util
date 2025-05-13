package io.github.chenfei0928.lang

import androidx.collection.ArrayMap
import androidx.collection.LruCache
import io.github.chenfei0928.base.UtilInitializer
import io.github.chenfei0928.collection.getOrPut
import io.github.chenfei0928.collection.mapToArray
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
import kotlin.reflect.jvm.jvmName

//<editor-fold desc="对任意对象进行toString支持" defaultstatus="collapsed">
fun Any?.toStringByReflect(): String = StringBuilder().appendByReflect(this).toString()

fun StringBuilder.appendByReflect(any: Any?): StringBuilder = when (any) {
    null -> append("null")
    is Class<*> -> if (any.isWriteByKotlin) {
        appendByReflect(any.kotlin)
    } else StaticFieldsCache.cache.getOrPut(any.name) {
        // java，打印当前类自身定义的static字段
        StaticFieldsCache.JavaClass(any)
    }.appendTo(this)
    is KClass<*> -> if (!any.java.isWriteByKotlin) {
        append(any.java)
    } else StaticFieldsCache.cache.getOrPut(any.jvmName) {
        // kotlin，打印伴生对象的字段
        val companionKls = if (any.isCompanion)
            any else any.companionObject
        if (companionKls != null) {
            StaticFieldsCache.KotlinKClassComponentObject(any, companionKls)
        } else {
            StaticFieldsCache.KotlinKClass(any)
        }
    }.appendTo(this)
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
        // 如果该类的 toString 方法被重写过（包括其父类）直接调用toString方法输出
        append(any.toString())
    } else appendObjectByReflectImpl(any)
}

/**
 * toString 方法是否被重写过的缓存
 */
private val toStringWasOverrideCache: MutableMap<Class<*>, Boolean> = ArrayMap()

/**
 * 类的静态字段缓存
 */
private sealed interface StaticFieldsCache {
    val clazzName: String

    fun appendTo(stringBuilder: StringBuilder): StringBuilder

    /**
     * java，打印当前类自身定义的static字段
     *
     * @param clazz 目标Java类实例
     */
    class JavaClass(
        private val clazz: Class<*>
    ) : StaticFieldsCache {
        override val clazzName: String = clazz.name
        private val fields: Array<Any> = clazz.declaredFields.filter {
            Modifier.isStatic(it.modifiers) && !it.isSynthetic
        }.mapToArray {
            it.isAccessible = true
            if (!Modifier.isFinal(it.modifiers)) it else it.name to it.get(null)
        }

        override fun appendTo(stringBuilder: StringBuilder): StringBuilder = stringBuilder
            .append(clazzName)
            .appendAnyFields(clazz, fields = fields)
    }

    /**
     * 没有伴生对象的Kotlin类
     *
     * @param any 目标Kotlin类实例
     */
    class KotlinKClass(
        any: KClass<*>
    ) : StaticFieldsCache {
        override val clazzName: String = any.qualifiedName!!
        override fun appendTo(stringBuilder: StringBuilder): StringBuilder =
            stringBuilder.append(clazzName).append("{}")
    }

    /**
     * kotlin，打印伴生对象的字段
     *
     * @param any 目标Kotlin类实例
     * @param companionKls 伴生类
     * @param companionObj 伴生类单例实例
     */
    class KotlinKClassComponentObject<T : Any>(
        any: KClass<*>,
        companionKls: KClass<T>,
        private val companionObj: T = companionKls.objectInstance!!
    ) : StaticFieldsCache {
        override val clazzName: String = any.qualifiedName!!
        private val fields = companionKls.declaredMemberProperties.mapToArray {
            it.isAccessible = true
            if (!it.isFinal) it else it.name to it.get(companionObj)
        }

        override fun appendTo(stringBuilder: StringBuilder): StringBuilder = stringBuilder
            .append(clazzName)
            .appendAnyFields(companionObj, fields = fields)
    }

    companion object {
        val cache = LruCache<String, StaticFieldsCache>(UtilInitializer.lruCacheStandardSize)
    }
}

private val classNonStaticFieldsCache = object : LruCache<Class<*>, List<Field>>(
    UtilInitializer.lruCacheStandardSize
) {
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

private fun StringBuilder.appendObjectByReflectImpl(any: Any) = apply {
    if (any.javaClass.isWriteByKotlin) {
        // 如果当前实例的类是kotlin类，且当前对象是伴生对象，尝试打印伴生对象的字段
        val kClass = any.javaClass.kotlin
        if (kClass.isCompanion) {
            StaticFieldsCache.cache.getOrPut(kClass.jvmName) {
                StaticFieldsCache.KotlinKClassComponentObject(kClass, kClass)
            }.appendTo(this)
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
//</editor-fold>

//<editor-fold desc="对实例toString字段支持" defaultstatus="collapsed">
fun Any.toStringAny(vararg fields: Any): String = StringBuilder()
    .append(this.javaClass.simpleName)
    .appendAnyFields(this, fields = fields)
    .toString()

fun StringBuilder.appendAnyFields(any: Any, vararg fields: Any): StringBuilder = apply {
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
                appendByReflect(getValue(any, value))
            }
            is KCallable<*> -> {
                append(field.name)
                append('=')
                appendByReflect(getValue(any, field))
            }
            is Field -> if (Modifier.isStatic(field.modifiers)) {
                field.get(null)
            } else {
                field.get(any)
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
//</editor-fold>
