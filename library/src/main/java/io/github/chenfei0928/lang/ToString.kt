package io.github.chenfei0928.lang

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
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
import io.github.chenfei0928.reflect.isSubclassOf
import io.github.chenfei0928.reflect.isWriteByKotlin
import io.github.chenfei0928.util.DependencyChecker
import io.github.chenfei0928.util.Log
import java.lang.invoke.MethodHandle
import java.lang.invoke.VarHandle
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
fun Any?.toStringByReflect(): String = StringBuilder().appendByReflectImpl(
    this, ToStringStackRecord(this, "this@toStringByReflect", null)
).toString()

fun StringBuilder.appendByReflect(
    any: Any?,
    record: ToStringStackRecord = ToStringStackRecord(any, "this@toStringByReflect", null),
): StringBuilder = appendByReflectImpl(any, record)

@Suppress("CyclomaticComplexMethod", "LongMethod")
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
    val clazzName: String

    fun appendTo(
        stringBuilder: StringBuilder, record: ToStringStackRecord
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
            stringBuilder: StringBuilder, record: ToStringStackRecord
        ): StringBuilder = stringBuilder
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
            stringBuilder: StringBuilder, record: ToStringStackRecord
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
            stringBuilder: StringBuilder, record: ToStringStackRecord
        ): StringBuilder = stringBuilder
            .append(clazzName)
            .appendAnyFieldsImpl(companionObj, record, fields = fields)
    }

    companion object {
        val cache = LruCache<String, StaticFieldsCache>(UtilInitializer.lruCacheStandardSize)
    }
}
//</editor-fold>

//<editor-fold desc="类实例字段缓存" defaultstatus="collapsed">
private sealed interface FieldsCache<T : Any> {
    val hasField: Boolean
    fun appendTo(
        builder: StringBuilder, any: T, record: ToStringStackRecord
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
            builder: StringBuilder, any: T, record: ToStringStackRecord
        ): StringBuilder = builder.apply {
            fields.forEach { field ->
                append(field.name)
                append('=')
                val value = getValue(any, field)
                if (value == any) {
                    append("this")
                } else {
                    appendByReflectImpl(value, record.onChildNode(value, field.name))
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
            builder: StringBuilder, any: T, record: ToStringStackRecord
        ): StringBuilder = builder.apply {
            fields.forEach { field ->
                append(field.name)
                append('=')
                val value = getValue(any, field)
                if (value == any) {
                    append("this")
                } else {
                    appendByReflectImpl(value, record.onChildNode(value, field.name))
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

fun StringBuilder.appendAnyFields(
    any: Any,
    record: ToStringStackRecord = ToStringStackRecord(any, "this@toStringAny", null),
    vararg fields: Any,
): StringBuilder = appendAnyFieldsImpl(any, record, fields = fields)

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
                appendByReflectImpl(
                    getValue(any, value),
                    record.onChildNode(value, key as? String ?: "[$index]-$key")
                )
            }
            // Kotlin 字段、方法
            is KCallable<*> -> {
                append(field.name)
                append('=')
                val value = getValue(any, field)
                appendByReflectImpl(value, record.onChildNode(value, field.name))
            }
            // Jvm反射体系的field
            is Field -> {
                append(field.name)
                append('=')
                val value = getValue(any, field)
                appendByReflectImpl(value, record.onChildNode(value, field.name))
            }
            // SpSaver preferenceDataStore的field
            is FieldAccessor.Field<*, *> -> {
                append(field.pdsKey)
                append('=')
                val value = getValue(any, field)
                appendByReflectImpl(value, record.onChildNode(value, field.pdsKey))
            }
            else -> {
                appendByReflectImpl(field, record.onChildNode(field, "[$index]"))
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
            is KProperty0<*> -> field.get()
            is KProperty1<*, *> -> (field as KProperty1<Any, *>).get(thisRef)
            else -> field
        }
    } catch (e: Exception) {
        "owner $thisRef's kProperty: $field get failed: $e"
    }
    // Kotlin方法
    is KFunction<*> -> try {
        when (field) {
            is Function0<*> -> field()
            is Function1<*, *> -> (field as Any.() -> Any)(thisRef)
            else -> field
        }
    } catch (e: Exception) {
        "owner $thisRef's func: $field invoke failed: $e"
    }
    // Jvm反射体系的field
    is Field -> try {
        if (Modifier.isStatic(field.modifiers)) {
            field.get(null)
        } else {
            field.get(thisRef)
        }
    } catch (e: Exception) {
        "owner $thisRef's field: $field get failed: $e"
    }
    // SpSaver preferenceDataStore的field
    is FieldAccessor.Field<*, *> -> (field as FieldAccessor.Field<Any, *>).get(thisRef)
    else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && field is MethodHandle) {
        // Jvm反射体系的methodHandle
        try {
            field.invoke(thisRef)
        } catch (e: Exception) {
            "owner $thisRef's methodHandle: $field invoke failed: $e"
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && field is VarHandle) {
        // Jvm反射体系的varHandle
        try {
            field.get(thisRef)
        } catch (e: Exception) {
            "owner $thisRef's varHandle: $field get failed: $e"
        }
    } else {
        field
    }
}
//</editor-fold>

fun StringBuilder.appendOrStd(any: Any?): StringBuilder = if (any == null) {
    append("null")
} else try {
    append(any.toString())
} catch (e: Exception) {
    append(any.javaClass.name)
    append('@')
    append(Integer.toHexString(any.hashCode()))
    append('(')
    append(e.toString())
    append(')')
}

fun Any.toStdString() = "${this::class.java.name}@${Integer.toHexString(this.hashCode())}"

//<editor-fold desc="ToString配置与过程记录器" defaultstatus"collapsed">
/**
 * 配置ToStringByReflect的参数
 *
 * @property useToString 是否使用对象的 [Any.toString] 方法
 * @property toStringWasOverrideCache toString 方法是否被重写过的缓存，默认为 ConcurrentHashMap。
 * 作为property以便于调试输出时可以看到是否重写了toString方法。
 * @property reflectSkipPackages 跳过反射处理字段的包名前缀，例如"java.lang."、"kotlin."等。
 * 在列表中的类型哪怕 [useToStringMethod] 返回`false`也会直接调用 [Any.toString] 方法输出，否则使用反射输出字段。
 * @property skipNodeTypes 跳过节点记录的类型，例如String、Int等，在列表中的类型每次都会 toString
 * @property protobufToShortString 是否在对protobuf的 [Message] 类型调用 [Message.toShortString] 方法，而不是 [Message.toString]
 * @property stringerProvider 自定义类型的toString方式，例如 protobuf 的 [Message] 类型可以使用 [Message.toShortString]
 */
data class ToStringByReflectConfig(
    private val useToString: Boolean,
    private val toStringWasOverrideCache: MutableMap<Class<*>, Boolean> =
        java.util.concurrent.ConcurrentHashMap(),
    private val reflectSkipPackages: Set<String>,
    private val skipNodeTypes: Set<Class<*>>,
    internal val protobufToShortString: Boolean,
    internal val stringerProvider: ((Any) -> Stringer<out Any>?)?,
    // 8个原生数组类型的toString方式
    internal val byteArrayStringer: Stringer<ByteArray>,
    internal val shortArrayStringer: Stringer<ShortArray>,
    internal val intArrayStringer: Stringer<IntArray>,
    internal val longArrayStringer: Stringer<LongArray>,
    internal val charArrayStringer: Stringer<CharArray>,
    internal val floatArrayStringer: Stringer<FloatArray>,
    internal val doubleArrayStringer: Stringer<DoubleArray>,
    internal val booleanArrayStringer: Stringer<BooleanArray>,
) {
    internal fun useToStringMethod(clazz: Class<*>): Boolean =
        useToString && toStringWasOverrideCache.getOrPut(clazz) {
            clazz.getMethod("toString").declaringClass != Any::class.java
        }

    /**
     * 是否跳过反射该类型的字段，如果跳过反射，则直接使用 [Any.toString] 方法
     */
    internal fun isReflectSkip(clazz: Class<*>): Boolean {
        return reflectSkipPackages.any { clazz.name.startsWith(it) }
    }

    /**
     * 是否跳过该类型的节点记录，例如String、Int等。如果跳过则每次都会调用 [appendByReflectImpl] 方法输出。
     * 如果不跳过，当某个对象重复出现时可能会在输出中见到其在其他节点的位置信息，而不是重复对其 toString。
     */
    internal fun isSkipNodeType(clazz: Class<*>): Boolean {
        return skipNodeTypes.any { clazz === it || clazz.isSubclassOf(it) }
    }

    //<editor-fold desc="原生数组类型toString方式" defaultstatus="collapsed">
    interface Stringer<T> {
        fun append(sb: StringBuilder, array: T): StringBuilder

        object ToStringOrStd : Stringer<Any> {
            override fun append(sb: StringBuilder, array: Any): StringBuilder =
                sb.appendOrStd(array)

            @Suppress("UNCHECKED_CAST")
            operator fun <T> invoke(): Stringer<T> =
                ToStringOrStd as Stringer<T>
        }

        sealed interface ArrayContentToString<T> : Stringer<T> {
            object Byte : ArrayContentToString<ByteArray> {
                override fun append(sb: StringBuilder, array: ByteArray): StringBuilder =
                    sb.append(array.contentToString())
            }

            object Short : ArrayContentToString<ShortArray> {
                override fun append(sb: StringBuilder, array: ShortArray): StringBuilder =
                    sb.append(array.contentToString())
            }

            object Int : ArrayContentToString<IntArray> {
                override fun append(sb: StringBuilder, array: IntArray): StringBuilder =
                    sb.append(array.contentToString())
            }

            object Long : ArrayContentToString<LongArray> {
                override fun append(sb: StringBuilder, array: LongArray): StringBuilder =
                    sb.append(array.contentToString())
            }

            object Char : ArrayContentToString<CharArray> {
                override fun append(sb: StringBuilder, array: CharArray): StringBuilder =
                    sb.append(array.contentToString())
            }

            object Float : ArrayContentToString<FloatArray> {
                override fun append(sb: StringBuilder, array: FloatArray): StringBuilder =
                    sb.append(array.contentToString())
            }

            object Double : ArrayContentToString<DoubleArray> {
                override fun append(sb: StringBuilder, array: DoubleArray): StringBuilder =
                    sb.append(array.contentToString())
            }

            object Boolean : ArrayContentToString<BooleanArray> {
                override fun append(sb: StringBuilder, array: BooleanArray): StringBuilder =
                    sb.append(array.contentToString())
            }
        }

        sealed interface ArraySizeToString<T> : Stringer<T> {
            object Byte : ArraySizeToString<ByteArray> {
                override fun append(sb: StringBuilder, array: ByteArray): StringBuilder =
                    sb.append("size:").append(array.size)
            }

            object Short : ArraySizeToString<ShortArray> {
                override fun append(sb: StringBuilder, array: ShortArray): StringBuilder =
                    sb.append("size:").append(array.size)
            }

            object Int : ArraySizeToString<IntArray> {
                override fun append(sb: StringBuilder, array: IntArray): StringBuilder =
                    sb.append("size:").append(array.size)
            }

            object Long : ArraySizeToString<LongArray> {
                override fun append(sb: StringBuilder, array: LongArray): StringBuilder =
                    sb.append("size:").append(array.size)
            }

            object Char : ArraySizeToString<CharArray> {
                override fun append(sb: StringBuilder, array: CharArray): StringBuilder =
                    sb.append("size:").append(array.size)
            }

            object Float : ArraySizeToString<FloatArray> {
                override fun append(sb: StringBuilder, array: FloatArray): StringBuilder =
                    sb.append("size:").append(array.size)
            }

            object Double : ArraySizeToString<DoubleArray> {
                override fun append(sb: StringBuilder, array: DoubleArray): StringBuilder =
                    sb.append("size:").append(array.size)
            }

            object Boolean : ArraySizeToString<BooleanArray> {
                override fun append(sb: StringBuilder, array: BooleanArray): StringBuilder =
                    sb.append("size:").append(array.size)
            }
        }

        sealed class HexToString<T>(
            private val bitCount: kotlin.Int
        ) : Stringer<T> {
            fun StringBuilder.appendHex(element: kotlin.Long): StringBuilder = apply {
                for (i in 0 until (bitCount / 4)) {
                    append(hexDigits[(element ushr (i * 4) and 0x0F).toInt()])
                }
            }

            object Byte : HexToString<ByteArray>(java.lang.Byte.SIZE) {
                override fun append(sb: StringBuilder, array: ByteArray): StringBuilder =
                    sb.apply { array.forEach { appendHex(it.toLong()) } }
            }

            object Short : HexToString<ShortArray>(java.lang.Short.SIZE) {
                override fun append(sb: StringBuilder, array: ShortArray): StringBuilder =
                    sb.apply { array.forEach { appendHex(it.toLong()) } }
            }

            object Int : HexToString<IntArray>(java.lang.Integer.SIZE) {
                override fun append(sb: StringBuilder, array: IntArray): StringBuilder =
                    sb.apply { array.forEach { appendHex(it.toLong()) } }
            }

            object Long : HexToString<LongArray>(java.lang.Long.SIZE) {
                override fun append(sb: StringBuilder, array: LongArray): StringBuilder =
                    sb.apply { array.forEach { appendHex(it) } }
            }

            object Char : HexToString<CharArray>(java.lang.Character.SIZE) {
                override fun append(sb: StringBuilder, array: CharArray): StringBuilder =
                    sb.apply { array.forEach { appendHex(it.code.toLong()) } }
            }

            object Float : HexToString<FloatArray>(java.lang.Float.SIZE) {
                override fun append(sb: StringBuilder, array: FloatArray): StringBuilder =
                    sb.apply { array.forEach { appendHex(it.toRawBits().toLong()) } }
            }

            object Double : HexToString<DoubleArray>(java.lang.Double.SIZE) {
                override fun append(sb: StringBuilder, array: DoubleArray): StringBuilder =
                    sb.apply { array.forEach { appendHex(it.toRawBits()) } }
            }

            object Boolean : HexToString<BooleanArray>(1) {
                override fun append(sb: StringBuilder, array: BooleanArray): StringBuilder =
                    sb.apply { array.forEach { append(if (it) '1' else '0') } }
            }

            companion object {
                private val hexDigits = charArrayOf(
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
                )
            }
        }
    }
    //</editor-fold>

    companion object {
        //<editor-fold desc="默认配置" defaultstatus="collapsed">
        val Default = ToStringByReflectConfig(
            useToString = true,
            reflectSkipPackages = setOf(
                "java.",
                "android.",
                "kotlin.",
                "kotlinx.",
            ),
            skipNodeTypes = setOf(
                java.lang.String::class.java,
                java.lang.CharSequence::class.java,
                java.lang.Number::class.java,
                java.lang.Boolean::class.java,
                java.lang.Byte::class.java,
                java.lang.Short::class.java,
                java.lang.Integer::class.java,
                java.lang.Long::class.java,
                java.lang.Float::class.java,
                java.lang.Double::class.java,
                java.lang.Character::class.java,
                java.lang.Void::class.java,
                java.util.Date::class.java,
            ),
            protobufToShortString = DependencyChecker.protobuf != null,
            stringerProvider = null,
            byteArrayStringer = Stringer.ArrayContentToString.Byte,
            shortArrayStringer = Stringer.ArrayContentToString.Short,
            intArrayStringer = Stringer.ArrayContentToString.Int,
            longArrayStringer = Stringer.ArrayContentToString.Long,
            charArrayStringer = Stringer.ArrayContentToString.Char,
            floatArrayStringer = Stringer.ArrayContentToString.Float,
            doubleArrayStringer = Stringer.ArrayContentToString.Double,
            booleanArrayStringer = Stringer.ArrayContentToString.Boolean,
        )
        //</editor-fold>
    }
}

/**
 * toStringByReflect 栈记录，用于记录对象到其之前出现过的位置的对应关系，避免无限递归。
 *
 * @property value 对象引用
 * @property nodeName 节点名称，用于记录该对象到其位置的对应关系
 * @property parentNode 父节点信息
 */
data class ToStringStackRecord(
    private val value: Any?,
    private val nodeName: String,
    private val parentNode: ToStringStackRecord?,
) {
    // 节点记录，用于记录对象到其之前出现过的位置的对应关系
    private val nodeRecords: MutableMap<Any, String> =
        parentNode?.nodeRecords ?: mutableMapOf()
    internal val config: ToStringByReflectConfig =
        parentNode?.config ?: UtilInitializer.toStringConfig

    internal fun onChildNode(value: Any?, name: String): ToStringStackRecord {
        return ToStringStackRecord(value, this.nodeName + "." + name, this)
    }

    internal fun findNodeNameByValue(value: Any): String? {
        return findParentNodeNameByValue(value) ?: findRecordedNodeName(value)
    }

    /**
     * 根据对象引用，查找其之前出现过的节点位置
     */
    private fun findParentNodeNameByValue(value: Any): String? {
        var currentNode: ToStringStackRecord? = parentNode
        while (currentNode != null) {
            if (currentNode.value === value)
                return currentNode.nodeName
            currentNode = currentNode.parentNode
        }
        return null
    }

    /**
     * 如果该对象在之前 toString 过，返回其之前出现过的节点位置，否则返回null
     */
    private fun findRecordedNodeName(value: Any): String? {
        val record = nodeRecords[value]
        if (record == null && value === this.value &&
            !config.isSkipNodeType(value.javaClass)
        ) {
            // 如果该对象在之前未记录过，则将其加入到节点记录中
            nodeRecords[value] = nodeName
        }
        return record
    }
}
//</editor-fold>
