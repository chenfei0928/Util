package io.github.chenfei0928.lang

import androidx.annotation.ReturnThis
import com.google.protobuf.Message
import com.google.protobuf.toShortString
import io.github.chenfei0928.base.UtilInitializer
import io.github.chenfei0928.reflect.isSubclassOf
import io.github.chenfei0928.util.DependencyChecker
import java.lang.Byte
import java.lang.CharSequence
import java.lang.Double
import java.lang.Float
import java.lang.Long
import java.lang.Number
import java.lang.Short
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import kotlin.Any
import kotlin.Boolean
import kotlin.BooleanArray
import kotlin.ByteArray
import kotlin.CharArray
import kotlin.DoubleArray
import kotlin.FloatArray
import kotlin.IntArray
import kotlin.LongArray
import kotlin.ShortArray
import kotlin.String
import kotlin.Suppress
import kotlin.apply
import kotlin.charArrayOf
import kotlin.code
import kotlin.toRawBits
import kotlin.toString

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
        ConcurrentHashMap(),
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
     * 是否跳过该类型的节点记录，例如String、Int等。
     * 如果跳过则每次都会调用 [appendByReflectImpl] 方法输出。
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

        @Suppress("UNUSED")
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

        @Suppress("UNUSED")
        sealed class HexToString<T>(
            private val bitCount: kotlin.Int
        ) : Stringer<T> {
            @ReturnThis
            fun StringBuilder.appendHex(element: kotlin.Long): StringBuilder {
                for (i in 0 until (bitCount / 4)) {
                    append(hexDigits[(element ushr (i * 4) and 0x0F).toInt()])
                }
                return this
            }

            object Byte : HexToString<ByteArray>(java.lang.Byte.SIZE) {
                override fun append(sb: StringBuilder, array: ByteArray): StringBuilder =
                    sb.apply { array.forEach { appendHex(it.toLong()) } }
            }

            object Short : HexToString<ShortArray>(java.lang.Short.SIZE) {
                override fun append(sb: StringBuilder, array: ShortArray): StringBuilder =
                    sb.apply { array.forEach { appendHex(it.toLong()) } }
            }

            object Int : HexToString<IntArray>(Integer.SIZE) {
                override fun append(sb: StringBuilder, array: IntArray): StringBuilder =
                    sb.apply { array.forEach { appendHex(it.toLong()) } }
            }

            object Long : HexToString<LongArray>(java.lang.Long.SIZE) {
                override fun append(sb: StringBuilder, array: LongArray): StringBuilder =
                    sb.apply { array.forEach { appendHex(it) } }
            }

            object Char : HexToString<CharArray>(Character.SIZE) {
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
                CharSequence::class.java,
                Number::class.java,
                java.lang.Boolean::class.java,
                Byte::class.java,
                Short::class.java,
                Integer::class.java,
                Long::class.java,
                Float::class.java,
                Double::class.java,
                Character::class.java,
                Void::class.java,
                Date::class.java,
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
