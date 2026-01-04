package io.github.chenfei0928.lang

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import androidx.collection.LruCache
import androidx.collection.SparseArrayCompat
import androidx.core.util.size
import io.github.chenfei0928.base.UtilInitializer
import io.github.chenfei0928.collection.getOrPut
import io.github.chenfei0928.collection.mapToArray
import io.github.chenfei0928.content.getAll
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
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmName

private const val TAG = "Ut_ToString"

//<editor-fold desc="对任意对象进行toString支持" defaultstatus="collapsed">
fun Any?.toStringByReflect(
    useToString: Boolean = true
): String = StringBuilder().appendByReflect(
    this, useToString, ToStringStackRecord(this, "this@toStringByReflect")
).toString()

@Suppress("CyclomaticComplexMethod", "LongMethod")
fun StringBuilder.appendByReflect(
    any: Any?, useToString: Boolean = true, record: ToStringStackRecord
): StringBuilder = if (any != null && record.findParentNodeNameByValue(any) != null) {
    append(record.findParentNodeNameByValue(any))
    this
} else when (any) {
    null -> append("null")
    // Java类对象
    is Class<*> -> if (any.isWriteByKotlin) {
        appendByReflect(any.kotlin, useToString, record)
    } else StaticFieldsCache.cache.getOrPut(any.name) {
        // java，打印当前类自身定义的static字段
        StaticFieldsCache.JavaClass(any)
    }.appendTo(this, useToString, record)
    // Kotlin类对象
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
    }.appendTo(this, useToString, record)
    // 数组
    is Array<*> -> {
        append('[')
        any.forEachIndexed { i, element ->
            if (i != 0) {
                append(", ")
            }
            appendByReflect(element, useToString, record.onChildNode(element, "[$i]"))
        }
        append(']')
    }
    is ByteArray -> append(if (UtilInitializer.toStringByReflectConfig.primitiveArrayContentToString) any.contentToString() else "size:${any.size}")
    is ShortArray -> append(if (UtilInitializer.toStringByReflectConfig.primitiveArrayContentToString) any.contentToString() else "size:${any.size}")
    is IntArray -> append(if (UtilInitializer.toStringByReflectConfig.primitiveArrayContentToString) any.contentToString() else "size:${any.size}")
    is LongArray -> append(if (UtilInitializer.toStringByReflectConfig.primitiveArrayContentToString) any.contentToString() else "size:${any.size}")
    is CharArray -> append(if (UtilInitializer.toStringByReflectConfig.primitiveArrayContentToString) any.contentToString() else "size:${any.size}")
    is FloatArray -> append(if (UtilInitializer.toStringByReflectConfig.primitiveArrayContentToString) any.contentToString() else "size:${any.size}")
    is DoubleArray -> append(if (UtilInitializer.toStringByReflectConfig.primitiveArrayContentToString) any.contentToString() else "size:${any.size}")
    is BooleanArray -> append(if (UtilInitializer.toStringByReflectConfig.primitiveArrayContentToString) any.contentToString() else "size:${any.size}")
    // JDK类型
    is CharSequence -> append(any)
    is Iterable<*> -> {
        append('[')
        any.forEachIndexed { i, element ->
            if (i != 0) {
                append(", ")
            }
            appendByReflect(element, useToString, record.onChildNode(element, "[$i]"))
        }
        append(']')
    }
    is Map<*, *> -> if (any.isEmpty()) {
        append(any.javaClass.simpleName)
        append("(empty)")
    } else {
        append('[')
        any.onEachIndexed { i, it ->
            appendByReflect(it.key, useToString, record.onChildNode(it.key, "[$i].key"))
            append('=')
            appendByReflect(it.value, useToString, record.onChildNode(it.value, "[$i].value"))
            append(", ")
        }
        replace(length - 2, length, "]")
    }
    is Reference<*> -> appendByReflect(
        any.get(),
        useToString,
        record.onChildNode(any.get(), "get()")
    )
    // 非JDK的容器类型
    is SparseArray<*> -> {
        append('[')
        for (i in 0 until any.size) {
            if (i != 0) append(", ")
            append(any.keyAt(i))
            append("=")
            val value = any.valueAt(i)
            appendByReflect(value, useToString, record.onChildNode(value, "[$i].value"))
        }
        append(']')
    }
    is SparseArrayCompat<*> -> {
        append('[')
        for (i in 0 until any.size()) {
            if (i != 0) append(", ")
            append(any.keyAt(i))
            append("=")
            val value = any.valueAt(i)
            appendByReflect(value, useToString, record.onChildNode(value, "[$i].value"))
        }
        append(']')
    }
    is Intent -> {
        append(any.toString())
        append(',')
        val all = any.extras?.getAll()
        appendByReflect(all, useToString = useToString, record.onChildNode(all, "extras"))
    }
    is Bundle -> {
        val all = any.getAll()
        appendByReflect(all, useToString = useToString, record.onChildNode(all, "getAll"))
    }
    // 判断该类有没有重写toString
    else -> if (useToString && toStringWasOverrideCache.getOrPut(any.javaClass) {
            any.javaClass.getMethod("toString").declaringClass != Any::class.java
        }) {
        // 如果该类的 toString 方法被重写过（包括其父类）直接调用toString方法输出
        try {
            append(any.toString())
        } catch (e: Exception) {
            append("${any.toStdString()}($e)")
        }
    } else appendObjectByReflectImpl(any, useToString, record)
}

/**
 * toString 方法是否被重写过的缓存
 */
private val toStringWasOverrideCache: MutableMap<Class<*>, Boolean> =
    java.util.concurrent.ConcurrentHashMap()

/**
 * 类的静态字段缓存
 */
private sealed interface StaticFieldsCache {
    val clazzName: String

    fun appendTo(
        stringBuilder: StringBuilder, useToString: Boolean, record: ToStringStackRecord
    ): StringBuilder

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

        override fun appendTo(
            stringBuilder: StringBuilder, useToString: Boolean, record: ToStringStackRecord
        ): StringBuilder = stringBuilder
            .append(clazzName)
            .appendAnyFields(clazz, useToString, record, fields = fields)
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
        override fun appendTo(
            stringBuilder: StringBuilder, useToString: Boolean, record: ToStringStackRecord
        ): StringBuilder = stringBuilder.append(clazzName).append("{}")
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

        // 获取这个类自身定义的非扩展字段，slow call
        // https://github.com/JetBrains/kotlin/blob/db825efc9f8cf648f33d30fb730d47a484519497/core/reflection.jvm/src/kotlin/reflect/full/KClasses.kt#L159
        private val fields = companionKls.declaredMemberProperties.mapToArray {
            it.isAccessible = true
            if (!it.isFinal) it else it.name to it.get(companionObj)
        }

        override fun appendTo(
            stringBuilder: StringBuilder, useToString: Boolean, record: ToStringStackRecord
        ): StringBuilder = stringBuilder
            .append(clazzName)
            .appendAnyFields(companionObj, useToString, record, fields = fields)
    }

    companion object {
        val cache = LruCache<String, StaticFieldsCache>(UtilInitializer.lruCacheStandardSize)
    }
}

private sealed interface FieldsCache<T : Any> {
    val hasField: Boolean
    fun appendTo(
        builder: StringBuilder, any: T, useToString: Boolean, record: ToStringStackRecord
    ): StringBuilder

    class Java<T : Any>(
        clazz: Class<T>
    ) : FieldsCache<T> {
        private val fields: List<Field> = clazz.declaredFields.filter {
            Modifier.TRANSIENT !in it.modifiers
                    && Modifier.STATIC !in it.modifiers
                    && !it.isSynthetic
        }.mapNotNull {
            try {
                it.isAccessible = true
                it
            } catch (e: Exception) {
                Log.d(TAG, "FieldsCache.Java.fields: ${it.name}", e)
                null
            }
        }
        override val hasField: Boolean = fields.isNotEmpty()

        override fun appendTo(
            builder: StringBuilder, any: T, useToString: Boolean, record: ToStringStackRecord
        ): StringBuilder = builder.apply {
            fields.forEach { field ->
                append(field.name)
                append('=')
                val value = getValue(any, field)
                if (value == any) {
                    append("this")
                } else {
                    appendByReflect(value, useToString, record.onChildNode(value, field.name))
                }
                append(", ")
            }
        }
    }

    class Kotlin<T : Any>(
        kClass: KClass<T>
    ) : FieldsCache<T> {
        // 获取该类及其父类自身定义的属性
        // https://github.com/JetBrains/kotlin/blob/db825efc9f8cf648f33d30fb730d47a484519497/core/reflection.jvm/src/kotlin/reflect/full/KClasses.kt#L145
        private val fields: List<KProperty1<T, *>> = kClass.memberProperties.mapNotNull {
            try {
                it.isAccessible = true
                it
            } catch (e: Exception) {
                Log.d(TAG, "FieldsCache.Kotlin.fields: ${it.name}", e)
                null
            }
        }
        override val hasField: Boolean = fields.isNotEmpty()

        override fun appendTo(
            builder: StringBuilder, any: T, useToString: Boolean, record: ToStringStackRecord
        ): StringBuilder = builder.apply {
            fields.forEach { field ->
                append(field.name)
                append('=')
                val value = getValue(any, field)
                if (value == any) {
                    append("this")
                } else {
                    appendByReflect(value, useToString, record.onChildNode(value, field.name))
                }
                append(", ")
            }
        }
    }

    companion object {
        val cache = LruCache<String, FieldsCache<*>>(UtilInitializer.lruCacheStandardSize)
    }
}

private fun StringBuilder.appendObjectByReflectImpl(
    any: Any, useToString: Boolean, record: ToStringStackRecord
) = apply {
    val thisClass: Class<*> = any.javaClass
    if (thisClass.isWriteByKotlin) {
        // 如果当前实例的类是kotlin类，且当前对象是伴生对象，尝试打印伴生对象的字段
        val kClass = thisClass.kotlin
        // slow call, 判断当前对象是不是伴生对象的实例
        if (kClass.isCompanion) {
            // 获取该伴生对象的宿主类
            val outerClass = thisClass.declaringClass
            StaticFieldsCache.cache.getOrPut(outerClass.name) {
                StaticFieldsCache.KotlinKClassComponentObject(outerClass.kotlin, kClass)
            }.appendTo(this, useToString, record)
            return@apply
        }
    }
    // 如果类在反射黑名单中，不处理这个类
    if (UtilInitializer.toStringByReflectConfig.isReflectSkip(thisClass)) {
        try {
            append(any.toString())
        } catch (e: Exception) {
            append("${any::class.java.name}@${Integer.toHexString(any.hashCode())}($e)")
        }
        return@apply
    }
    // 不是数组，toString 也没有被重写过，调用反射输出每一个字段
    var thisOrSuperClass: Class<*>? = thisClass
    append(thisOrSuperClass?.simpleName)
    append('(')
    var hasAnyField = false
    while (thisOrSuperClass != null && thisOrSuperClass != Any::class.java) {
        // 打印当前类的非static字段
        if (thisOrSuperClass.isWriteByKotlin) {
            // Kotlin会输出所有字段，包括父类的
            @Suppress("UNCHECKED_CAST")
            val cache: FieldsCache<Any> = FieldsCache.cache.getOrPut(thisOrSuperClass.name) {
                FieldsCache.Kotlin(thisOrSuperClass.kotlin)
            } as FieldsCache<Any>
            cache.appendTo(this, any, useToString, record)
            hasAnyField = hasAnyField or cache.hasField
            break
        } else {
            // Java只会输出当前类的
            @Suppress("UNCHECKED_CAST")
            val cache: FieldsCache<Any> = FieldsCache.cache.getOrPut(thisOrSuperClass.name) {
                FieldsCache.Java(thisOrSuperClass)
            } as FieldsCache<Any>
            cache.appendTo(this, any, useToString, record)
            hasAnyField = hasAnyField or cache.hasField
            thisOrSuperClass = thisOrSuperClass.getSuperclass()
        }
    }
    if (hasAnyField) {
        replace(length - 2, length, ")")
    } else {
        append(')')
    }
}
//</editor-fold>

//<editor-fold desc="对实例toString字段支持" defaultstatus="collapsed">
fun Any.toStringAny(useToString: Boolean = true, vararg fields: Any): String = StringBuilder()
    .append(this.javaClass.simpleName)
    .appendAnyFields(
        this, useToString, ToStringStackRecord(this, "this@toStringAny"), fields = fields
    )
    .toString()

fun StringBuilder.appendAnyFields(
    any: Any, useToString: Boolean = true, record: ToStringStackRecord, vararg fields: Any
): StringBuilder = apply {
    append('(')
    fields.forEachIndexed { index, field ->
        if (index != 0) {
            append(", ")
        }
        when (field) {
            // name to Kotlin字段、方法
            is Pair<*, *> -> {
                val (key, value) = field
                append(key)
                append('=')
                appendByReflect(
                    getValue(any, value), useToString, record.onChildNode(value, "[$index]-$key")
                )
            }
            // Kotlin 字段、方法
            is KCallable<*> -> {
                append(field.name)
                append('=')
                val value = getValue(any, field)
                appendByReflect(
                    value,
                    useToString,
                    record.onChildNode(value, "[$index]-${field.name}")
                )
            }
            // Jvm反射体系的field
            is Field -> {
                append(field.name)
                append('=')
                val value = getValue(any, field)
                appendByReflect(
                    value,
                    useToString,
                    record.onChildNode(value, "[$index]-${field.name}")
                )
            }
            // SpSaver preferenceDataStore的field
            is FieldAccessor.Field<*, *> -> {
                append(field.pdsKey)
                append('=')
                val value = getValue(any, field)
                appendByReflect(
                    value,
                    useToString,
                    record.onChildNode(value, "[$index]-${field.pdsKey}")
                )
            }
            else -> {
                appendByReflect(field, useToString, record.onChildNode(field, "[$index]"))
            }
        }
    }
    append(')')
}

@Suppress("UNCHECKED_CAST")
private fun getValue(
    thisRef: Any, field: Any?,
): Any? = when (field) {
    // Kotlin字段
    is KProperty<*> -> try {
        when (field) {
            is KProperty0<*> ->
                field.get()
            is KProperty1<*, *> ->
                (field as KProperty1<Any, *>).get(thisRef)
            else -> field
        }
    } catch (e: Exception) {
        "owner $thisRef's field: $field get error: $e"
    }
    // Kotlin方法
    is KFunction<*> -> try {
        when (field) {
            is Function0<*> ->
                field()
            is Function1<*, *> ->
                (field as Any.() -> Any)(thisRef)

            else -> field
        }
    } catch (e: Exception) {
        "owner $thisRef's func: $field invoke error: $e"
    }
    // Jvm反射体系的field
    is Field -> try {
        if (Modifier.isStatic(field.modifiers)) {
            field.get(null)
        } else {
            field.get(thisRef)
        }
    } catch (e: Exception) {
        "owner $thisRef's field: $field get error: $e"
    }
    // SpSaver preferenceDataStore的field
    is FieldAccessor.Field<*, *> -> {
        (field as FieldAccessor.Field<Any, *>).get(thisRef)
    }
    else -> field
}
//</editor-fold>

fun Any.toStdString() = "${this::class.java.name}@${Integer.toHexString(this.hashCode())}"

data class ToStringConfig(
    var primitiveArrayContentToString: Boolean = true,
    val reflectSkipPackages: Set<String>,
) {
    fun isReflectSkip(clazz: Class<*>): Boolean {
        return reflectSkipPackages.any { clazz.name.startsWith(it) }
    }

    companion object {
        val Default = ToStringConfig(
            true,
            setOf(
                "java.",
                "android.",
                "kotlin.",
                "kotlinx.",
            )
        )
    }
}

data class ToStringStackRecord(
    val value: Any?,
    val nodeName: String,
    var parentNode: ToStringStackRecord? = null,
) {
    fun onChildNode(value: Any?, name: String): ToStringStackRecord {
        val child = ToStringStackRecord(value, this.nodeName + "." + name)
        child.parentNode = this
        return child
    }

    fun findParentNodeNameByValue(value: Any): String? {
        var currentNode: ToStringStackRecord? = parentNode
        while (currentNode != null) {
            if (currentNode.value === value)
                return currentNode.nodeName
            currentNode = currentNode.parentNode
        }
        return null
    }
}
