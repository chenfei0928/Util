@file:Suppress("TooManyFunctions")

package io.github.chenfei0928.lang

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import androidx.annotation.ReturnThis
import androidx.collection.LruCache
import androidx.collection.SparseArrayCompat
import androidx.core.util.size
import com.google.protobuf.Message
import com.google.protobuf.toShortString
import io.github.chenfei0928.base.UtilInitializer
import io.github.chenfei0928.collection.getOrPut
import io.github.chenfei0928.collection.mapToArray
import io.github.chenfei0928.content.getAll
import io.github.chenfei0928.preference.base.FieldAccessor
import io.github.chenfei0928.reflect.isFinal
import io.github.chenfei0928.reflect.isStatic
import io.github.chenfei0928.reflect.isTransient
import io.github.chenfei0928.reflect.isWriteByKotlin
import io.github.chenfei0928.util.Log
import java.lang.invoke.MethodHandle
import java.lang.invoke.VarHandle
import java.lang.ref.Reference
import java.lang.reflect.Field
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
fun Any?.toStringByReflect(): String = StringBuilder().appendByReflectImpl(
    this, ToStringStackRecord(this, "this@toStringByReflect", null)
).toString()

@ReturnThis
fun StringBuilder.appendByReflect(
    any: Any?,
    record: ToStringStackRecord = ToStringStackRecord(any, "this@toStringByReflect", null),
): StringBuilder = appendByReflectImpl(any, record)

@ReturnThis
@Suppress("CyclomaticComplexMethod", "LongMethod", "ReturnCount")
private fun StringBuilder.appendByReflectImpl(
    any: Any?, record: ToStringStackRecord,
): StringBuilder {
    if (any == null) {
        return append("null")
    }
    // 检查这个字段在之前有没有出现过，如果有就直接用它的节点信息代替
    val nodeNameByValue = record.findNodeNameByValue(any)
    if (nodeNameByValue != null) {
        return append(nodeNameByValue)
    }
    // 获取自定义的toString方法
    @Suppress("UNCHECKED_CAST")
    val stringer = record.config.stringerProvider
        ?.invoke(any) as? ToStringByReflectConfig.Stringer<Any>
    if (stringer != null) {
        return stringer.append(this, any)
    }
    return when (any) {
        // Java类对象
        is Class<*> -> if (any.isWriteByKotlin) {
            appendByReflectImpl(any.kotlin, record)
        } else StaticFieldsCache.cache.getOrPut(any.name) {
            // java，打印当前类自身定义的static字段
            StaticFieldsCache.JavaClass(any)
        }.appendTo(this, record)
        // Kotlin类对象
        is KClass<*> -> if (!any.java.isWriteByKotlin) {
            appendByReflectImpl(any.java, record)
        } else StaticFieldsCache.cache.getOrPut(any.jvmName) {
            // kotlin，打印伴生对象的字段
            val companionKls = if (any.isCompanion)
                any else any.companionObject
            if (companionKls != null) {
                StaticFieldsCache.KotlinKClassComponentObject(any, companionKls)
            } else {
                StaticFieldsCache.KotlinKClass(any)
            }
        }.appendTo(this, record)
        // 数组
        is Array<*> -> {
            append('[')
            any.forEachIndexed { i, element ->
                if (i != 0) {
                    append(", ")
                }
                appendByReflectImpl(element, record.onChildNode(element, "[$i]"))
            }
            append(']')
        }
        is ByteArray -> record.config.byteArrayStringer.append(this, any)
        is ShortArray -> record.config.shortArrayStringer.append(this, any)
        is IntArray -> record.config.intArrayStringer.append(this, any)
        is LongArray -> record.config.longArrayStringer.append(this, any)
        is CharArray -> record.config.charArrayStringer.append(this, any)
        is FloatArray -> record.config.floatArrayStringer.append(this, any)
        is DoubleArray -> record.config.doubleArrayStringer.append(this, any)
        is BooleanArray -> record.config.booleanArrayStringer.append(this, any)
        // JDK类型
        is CharSequence -> append(any)
        is Iterable<*> -> {
            append('[')
            any.forEachIndexed { i, element ->
                if (i != 0) {
                    append(", ")
                }
                appendByReflectImpl(element, record.onChildNode(element, "[$i]"))
            }
            append(']')
        }
        is Map<*, *> -> if (any.isEmpty()) {
            append(any.javaClass.simpleName)
            append("(empty)")
        } else {
            append('[')
            any.onEachIndexed { i, entry ->
                appendByReflectImpl(entry.key, record.onChildNode(entry.key, "[$i].key"))
                append('=')
                appendByReflectImpl(entry.value, record.onChildNode(entry.value, "[$i].value"))
                append(", ")
            }
            replace(length - 2, length, "]")
        }
        is Map.Entry<*, *> -> {
            appendByReflectImpl(any.key, record.onChildNode(any.key, "key"))
            append('=')
            appendByReflectImpl(any.value, record.onChildNode(any.value, "value"))
        }
        is Reference<*> -> appendByReflectImpl(any.get(), record.onChildNode(any.get(), "get()"))
        // 非JDK的容器类型
        is SparseArray<*> -> {
            append('[')
            for (i in 0 until any.size) {
                if (i != 0) append(", ")
                append(any.keyAt(i))
                append("=")
                val value = any.valueAt(i)
                appendByReflectImpl(value, record.onChildNode(value, "[$i].value"))
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
                appendByReflectImpl(value, record.onChildNode(value, "[$i].value"))
            }
            append(']')
        }
        is Intent -> {
            append(any.toString())
            append(',')
            val all = any.extras?.getAll()
            appendByReflectImpl(all, record.onChildNode(all, "extras"))
        }
        is Bundle -> {
            val all = any.getAll()
            appendByReflectImpl(all, record.onChildNode(all, "getAll"))
        }
        // 判断该类有没有重写toString
        else -> if (record.config.protobufToShortString && any is Message) {
            // protobuf 序列化对象
            append(any.toShortString())
        } else if (!record.config.useToStringMethod(any.javaClass)) {
            // 如果该类的 toString 方法没有被重写过（包括其父类）则反射输出字段
            appendObjectByReflectImpl(any, record)
        } else {
            // 如果该类的 toString 方法被重写过（包括其父类）直接调用toString方法输出
            appendOrStd(any)
        }
    }
}

//<editor-fold desc="静态字段缓存（JClass、KClass、KComponent）" defaultstatus="collapsed">
/**
 * 类的静态字段缓存
 */
private sealed interface StaticFieldsCache {
    /**
     * 类名信息
     */
    val clazzName: String

    /**
     * 将当前类静态字段输出到 [builder] 中，并返回该字符串构建器实例
     *
     * @param builder 字符串构建器实例
     * @param record 记录堆栈信息，用于递归调用时防止无限循环
     * @return [builder]实例
     */
    fun appendTo(
        builder: StringBuilder, record: ToStringStackRecord
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
            it.isStatic && !it.isSynthetic
        }.mapToArray {
            it.isAccessible = true
            if (!it.isFinal) it else it.name to it.get(null)
        }

        override fun appendTo(
            builder: StringBuilder, record: ToStringStackRecord
        ): StringBuilder = builder
            .append(clazzName)
            .appendAnyFieldsImpl(clazz, record, fields = fields)
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
            builder: StringBuilder, record: ToStringStackRecord
        ): StringBuilder = builder.append(clazzName).append("{}")
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
            builder: StringBuilder, record: ToStringStackRecord
        ): StringBuilder = builder
            .append(clazzName)
            .appendAnyFieldsImpl(companionObj, record, fields = fields)
    }

    companion object {
        val cache = LruCache<String, StaticFieldsCache>(UtilInitializer.lruCacheStandardSize)
    }
}
//</editor-fold>

//<editor-fold desc="类实例字段缓存" defaultstatus="collapsed">
/**
 * 类实例字段缓存，不包括transient和static字段。
 *
 * 用于Java类或Kotlin类的toString方法实现。
 *
 * @param T 类类型
 */
private sealed interface FieldsCache<T : Any> {
    /**
     * 该类是否有成员字段可以被输出。
     */
    val hasField: Boolean

    /**
     * 将当前类实例字段输出到 [builder] 中，并返回该字符串构建器实例。
     *
     * @param builder 字符串构建器实例
     * @param any 类实例
     * @param record 记录堆栈信息，用于递归调用时防止无限循环。
     * @return [builder]实例。
     */
    fun appendTo(
        builder: StringBuilder, any: T, record: ToStringStackRecord
    ): StringBuilder

    /**
     * Java类字段缓存，不包括transient和static字段。
     *
     * 用于Java类实例的toString方法实现。
     *
     * @param clazz Java类对象
     */
    class Java<T : Any>(
        clazz: Class<T>
    ) : FieldsCache<T> {
        private val fields: List<Field> = clazz.declaredFields.filter {
            !it.isTransient && !it.isStatic && !it.isSynthetic
        }.mapNotNull {
            try {
                it.isAccessible = true
                it
            } catch (e: SecurityException) {
                Log.d(TAG, "FieldsCache.Java.fields: ${it.name}", e)
                null
            }
        }
        override val hasField: Boolean = fields.isNotEmpty()

        override fun appendTo(
            builder: StringBuilder, any: T, record: ToStringStackRecord
        ): StringBuilder = builder.apply {
            fields.forEach { field ->
                append(field.name)
                append('=')
                appendValue(any, field) { value ->
                    if (value == any) {
                        append("this")
                    } else {
                        appendByReflectImpl(value, record.onChildNode(value, field.name))
                    }
                }
                append(", ")
            }
        }
    }

    /**
     * 根据Kotlin类对象获取其字段列表，但不包括扩展属性。
     *
     * 用于Kotlin类实例的toString方法实现。
     *
     * @param kClass Kotlin类对象
     */
    class Kotlin<T : Any>(
        kClass: KClass<T>
    ) : FieldsCache<T> {
        // 获取该类及其父类自身定义的属性
        // https://github.com/JetBrains/kotlin/blob/db825efc9f8cf648f33d30fb730d47a484519497/core/reflection.jvm/src/kotlin/reflect/full/KClasses.kt#L145
        private val fields: List<KProperty1<T, *>> = kClass.memberProperties.mapNotNull {
            try {
                it.isAccessible = true
                it
            } catch (e: SecurityException) {
                Log.d(TAG, "FieldsCache.Kotlin.fields: ${it.name}", e)
                null
            }
        }
        override val hasField: Boolean = fields.isNotEmpty()

        override fun appendTo(
            builder: StringBuilder, any: T, record: ToStringStackRecord
        ): StringBuilder = builder.apply {
            fields.forEach { field ->
                append(field.name)
                append('=')
                appendValue(any, field) { value ->
                    if (value == any) {
                        append("this")
                    } else {
                        appendByReflectImpl(value, record.onChildNode(value, field.name))
                    }
                }
                append(", ")
            }
        }
    }

    companion object {
        val cache = LruCache<String, FieldsCache<*>>(UtilInitializer.lruCacheStandardSize)
    }
}
//</editor-fold>

@ReturnThis
private fun StringBuilder.appendObjectByReflectImpl(
    any: Any, record: ToStringStackRecord
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
            }.appendTo(this, record)
            return@apply
        }
    }
    // 如果类在反射黑名单中，不使用反射处理这个类
    if (record.config.isReflectSkip(thisClass)) {
        appendOrStd(any)
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
            cache.appendTo(this, any, record)
            hasAnyField = hasAnyField or cache.hasField
            break
        } else {
            // Java只会输出当前类的
            @Suppress("UNCHECKED_CAST")
            val cache: FieldsCache<Any> = FieldsCache.cache.getOrPut(thisOrSuperClass.name) {
                FieldsCache.Java(thisOrSuperClass)
            } as FieldsCache<Any>
            cache.appendTo(this, any, record)
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
fun Any.toStringAny(vararg fields: Any): String = StringBuilder()
    .append(this.javaClass.simpleName)
    .appendAnyFieldsImpl(
        this, ToStringStackRecord(this, "this@toStringAny", null), fields = fields
    )
    .toString()

@ReturnThis
fun StringBuilder.appendAnyFields(
    any: Any,
    record: ToStringStackRecord = ToStringStackRecord(any, "this@toStringAny", null),
    vararg fields: Any,
): StringBuilder = appendAnyFieldsImpl(any, record, fields = fields)

@ReturnThis
private fun StringBuilder.appendAnyFieldsImpl(
    any: Any,
    record: ToStringStackRecord,
    vararg fields: Any,
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
                appendOrStd(key)
                append('=')
                appendValue(any, value) { value ->
                    appendByReflectImpl(
                        value,
                        record.onChildNode(value, key as? String ?: "[$index]-$key")
                    )
                }
            }
            // Kotlin 字段、方法
            is KCallable<*> -> {
                append(field.name)
                append('=')
                appendValue(any, field) { value ->
                    appendByReflectImpl(value, record.onChildNode(value, field.name))
                }
            }
            // Jvm反射体系的field
            is Field -> {
                append(field.name)
                append('=')
                appendValue(any, field) { value ->
                    appendByReflectImpl(value, record.onChildNode(value, field.name))
                }
            }
            // SpSaver preferenceDataStore的field
            is FieldAccessor.Field<*, *> -> {
                append(field.pdsKey)
                append('=')
                appendValue(any, field) { value ->
                    appendByReflectImpl(value, record.onChildNode(value, field.pdsKey))
                }
            }
            else -> {
                appendByReflectImpl(field, record.onChildNode(field, "[$index]"))
            }
        }
    }
    append(')')
}

//<editor-fold desc="对JvmField类型为值类型的append处理" defaultstatus="collapsed">
@ReturnThis
private inline fun StringBuilder.appendValue(
    thisRef: Any,
    field: Any?,
    boxedOrObjectValueAppendable: StringBuilder.(value: Any?) -> StringBuilder
): StringBuilder = if (field is Field && field.type.isPrimitive) {
    appendPrimitiveValue(thisRef, field)
} else {
    boxedOrObjectValueAppendable(getValue(thisRef, field))
}

@ReturnThis
private fun StringBuilder.appendPrimitiveValue(
    thisRef: Any, field: Field,
): StringBuilder = try {
    val param = if (field.isStatic) null else thisRef
    @Suppress("RemoveRedundantQualifierName")
    when (field.type) {
        java.lang.Integer.TYPE -> append(field.getInt(param))
        java.lang.Float.TYPE -> append(field.getFloat(param))
        java.lang.Byte.TYPE -> append(field.getByte(param))
        java.lang.Double.TYPE -> append(field.getDouble(param))
        java.lang.Long.TYPE -> append(field.getLong(param))
        java.lang.Character.TYPE -> append(field.getChar(param))
        java.lang.Boolean.TYPE -> append(field.getBoolean(param))
        java.lang.Short.TYPE -> append(field.getShort(param))
        else -> throw IllegalArgumentException("field type ${field.type} not supported: $field")
    }
} catch (e: IllegalArgumentException) {
    append("owner $thisRef's field: $field get failed: $e")
} catch (e: IllegalAccessException) {
    append("owner $thisRef's field: $field get failed: $e")
}
//</editor-fold>

@Suppress("TooGenericExceptionCaught", "CyclomaticComplexMethod")
private fun getValue(
    thisRef: Any, field: Any?,
): Any? = try {
    when {
        // Kotlin字段
        field is KProperty<*> -> when (field) {
            is KProperty0<*> -> field.get()
            is KProperty1<*, *> ->
                @Suppress("UNCHECKED_CAST")
                (field as KProperty1<Any, *>).get(thisRef)
            else -> field
        }
        // Kotlin方法
        field is KFunction<*> -> when (field) {
            is Function0<*> -> field()
            is Function1<*, *> ->
                @Suppress("UNCHECKED_CAST")
                (field as Any.() -> Any)(thisRef)
            else -> field
        }
        // Jvm反射体系的field
        field is Field -> field.get(if (field.isStatic) null else thisRef)
        // SpSaver preferenceDataStore的field
        field is FieldAccessor.Field<*, *> ->
            @Suppress("UNCHECKED_CAST")
            (field as FieldAccessor.Field<Any, *>).get(thisRef)
        // Jvm反射体系的methodHandle
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && field is MethodHandle ->
            field.invoke(thisRef)
        // Jvm反射体系的varHandle
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && field is VarHandle ->
            field.get(thisRef)
        else -> field
    }
} catch (e: Throwable) {
    "owner $thisRef's $field get/invoke failed: $e"
}
//</editor-fold>

@ReturnThis
@Suppress("TooGenericExceptionCaught")
fun StringBuilder.appendOrStd(any: Any?): StringBuilder = if (any == null) {
    append("null")
} else try {
    append(any.toString())
} catch (e: Throwable) {
    append(any.javaClass.name)
    append('@')
    append(Integer.toHexString(any.hashCode()))
    append('(')
    append(e.toString())
    append(')')
}

fun Any.toStdString() = "${this::class.java.name}@${Integer.toHexString(this.hashCode())}"
