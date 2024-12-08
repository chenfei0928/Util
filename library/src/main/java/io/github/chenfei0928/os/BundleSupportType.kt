package io.github.chenfei0928.os

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.util.Size
import android.util.SizeF
import android.util.SparseArray
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import com.google.protobuf.getProtobufLiteDefaultInstance
import com.google.protobuf.getProtobufV3DefaultInstance
import io.github.chenfei0928.collection.asArrayList
import io.github.chenfei0928.lang.contains
import io.github.chenfei0928.lang.toByteArray
import io.github.chenfei0928.reflect.isSubclassOf
import io.github.chenfei0928.util.DependencyChecker
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.Serializable
import java.lang.reflect.Modifier
import kotlin.Enum
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * 子类实现要求重写[nonnullValue]提供默认值，重写[putNonnull]来对[Bundle]进行写入数据，
 * 并根据获取值get方法获取数据返回值的可空性重写[getNonnull]、[getExtraNonnull]
 * 或[getNullable]、[getExtraNullable]来返回数据。
 *
 * @author chenf()
 * @date 2024-12-05 11:01
 */
abstract class BundleSupportType<T>(
    private val isMarkedNullable: Boolean?
) {

    //<editor-fold desc="接口API信息" defaultstatus="collapsed">
    /**
     * 返回非空默认数据
     */
    protected abstract fun nonnullValue(property: KProperty<*>): T & Any

    //<editor-fold desc="Bundle的put" defaultstatus="collapsed">
    /**
     * 填充数据，如果[value]是null，将数据直接[Bundle.remove]，否则调用[putNonnull]存放数据
     */
    fun putNullable(
        bundle: Bundle, property: KProperty<*>, name: String, value: T?
    ) = if (value == null) bundle.remove(name) else putNonnull(bundle, property, name, value)

    protected abstract fun putNonnull(
        bundle: Bundle, property: KProperty<*>, name: String, value: T & Any
    )
    //</editor-fold>

    //<editor-fold desc="Bundle的get"  defaultstatus="collapsed">
    /**
     * 对外暴露的对[Bundle]获取值的方法
     *
     * 会根据构造器传入的[isMarkedNullable]或[property]的可空性调用对应返回值的方法
     *
     * 子类需要根据其数据类型在[Bundle]中get方法返回的可空性重写[getExtraNonnull]或[getExtraNullable]方法
     *
     * @param defaultValue 如果数据源[bundle]中没有数据时，且[isMarkedNullable]为false不可空时返回的默认值
     */
    fun getValue(
        bundle: Bundle, property: KProperty<*>, name: String, defaultValue: T?
    ): T = if (isMarkedNullable ?: property.returnType.isMarkedNullable)
        getNullable(bundle, property, name)!!
    else getNonnull(bundle, property, name, defaultValue)

    /**
     * 获取[Intent]的扩展数据，并返回可空数据。如果数据中没有存储该数据，则返回null
     */
    open fun getNullable(bundle: Bundle, property: KProperty<*>, name: String): T? =
        if (!bundle.containsKey(name)) null else getNonnull(bundle, property, name, null)

    /**
     * 获取[Intent]的扩展数据，并返回非空数据。如果数据中没有存储该数据，则返回默认数据
     */
    open fun getNonnull(
        bundle: Bundle, property: KProperty<*>, name: String, defaultValue: T?
    ): T = if (!bundle.containsKey(name)) defaultValue ?: nonnullValue(property) else
        getNullable(bundle, property, name) ?: defaultValue ?: nonnullValue(property)
    //</editor-fold>

    //<editor-fold desc="Intent的get" defaultstatus="collapsed">
    /**
     * 对外暴露的对[Intent]获取值的方法
     *
     * 会根据构造器传入的[isMarkedNullable]或[property]的可空性调用对应返回值的方法
     *
     * 子类需要根据其数据类型在[Intent]中get方法返回的可空性重写[getExtraNonnull]或[getExtraNullable]方法
     *
     * @param defaultValue 如果数据源[intent]中没有数据时，且[isMarkedNullable]为false不可空时返回的默认值
     */
    fun getValue(
        intent: Intent, property: KProperty<*>, name: String, defaultValue: T?
    ): T = if (isMarkedNullable ?: property.returnType.isMarkedNullable)
        getExtraNullable(intent, property, name)!!
    else getExtraNonnull(intent, property, name, defaultValue)

    /**
     * 获取[Intent]的扩展数据，并返回可空数据。如果数据中没有存储该数据，则返回null
     */
    open fun getExtraNullable(intent: Intent, property: KProperty<*>, name: String): T? =
        if (!intent.hasExtra(name)) null else getExtraNonnull(intent, property, name, null)

    /**
     * 获取[Intent]的扩展数据，并返回非空数据。如果数据中没有存储该数据，则返回默认数据
     */
    open fun getExtraNonnull(
        intent: Intent, property: KProperty<*>, name: String, defaultValue: T?
    ): T = if (!intent.hasExtra(name)) defaultValue ?: nonnullValue(property) else
        getExtraNullable(intent, property, name) ?: defaultValue ?: nonnullValue(property)
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="基础数据类型与其数组" defaultstatus="collapsed">
    class ByteType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<Byte>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Byte = 0
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Byte
        ) = bundle.putByte(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Byte?
        ): Byte = bundle.getByte(name, 0)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Byte?
        ): Byte = intent.getByteExtra(name, 0)

        companion object : AutoFind.Creator<Byte> {
            override val default = ByteType(null)
            override fun byType(kType: KType): BundleSupportType<Byte> =
                ByteType(kType.isMarkedNullable)
        }
    }

    class ByteArrayType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<ByteArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): ByteArray = byteArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: ByteArray
        ) = bundle.putByteArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): ByteArray? = bundle.getByteArray(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): ByteArray? = intent.getByteArrayExtra(name)

        companion object : AutoFind.Creator<ByteArray> {
            override val default = ByteArrayType(null)
            override fun byType(kType: KType): BundleSupportType<ByteArray> =
                ByteArrayType(kType.isMarkedNullable)
        }
    }

    class ShortType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<Short>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Short = 0
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Short
        ) = bundle.putShort(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Short?
        ): Short = bundle.getShort(name, defaultValue ?: 0)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Short?
        ): Short = intent.getShortExtra(name, defaultValue ?: 0)

        companion object : AutoFind.Creator<Short> {
            override val default = ShortType(null)
            override fun byType(kType: KType): BundleSupportType<Short> =
                ShortType(kType.isMarkedNullable)
        }
    }

    class ShortArrayType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<ShortArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): ShortArray = shortArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: ShortArray
        ) = bundle.putShortArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): ShortArray? = bundle.getShortArray(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): ShortArray? = intent.getShortArrayExtra(name)

        companion object : AutoFind.Creator<ShortArray> {
            override val default = ShortArrayType(null)
            override fun byType(kType: KType): BundleSupportType<ShortArray> =
                ShortArrayType(kType.isMarkedNullable)
        }
    }

    class IntType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<Int>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Int = 0
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Int
        ) = bundle.putInt(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Int?
        ): Int = bundle.getInt(name, defaultValue ?: 0)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Int?
        ): Int = intent.getIntExtra(name, defaultValue ?: 0)

        companion object : AutoFind.Creator<Int> {
            override val default = IntType(null)
            override fun byType(kType: KType): BundleSupportType<Int> =
                IntType(kType.isMarkedNullable)
        }
    }

    class IntArrayType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<IntArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): IntArray = intArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: IntArray
        ) = bundle.putIntArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): IntArray? = bundle.getIntArray(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): IntArray? = intent.getIntArrayExtra(name)

        companion object : AutoFind.Creator<IntArray> {
            override val default = IntArrayType(null)
            override fun byType(kType: KType): BundleSupportType<IntArray> =
                IntArrayType(kType.isMarkedNullable)
        }
    }

    class LongType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<Long>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Long = 0
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Long
        ) = bundle.putLong(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Long?
        ): Long = bundle.getLong(name, 0)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Long?
        ): Long = intent.getLongExtra(name, 0)

        companion object : AutoFind.Creator<Long> {
            override val default = LongType(null)
            override fun byType(kType: KType): BundleSupportType<Long> =
                LongType(kType.isMarkedNullable)
        }
    }

    class LongArrayType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<LongArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): LongArray = longArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: LongArray
        ) = bundle.putLongArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): LongArray? = bundle.getLongArray(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): LongArray? = intent.getLongArrayExtra(name)

        companion object : AutoFind.Creator<LongArray> {
            override val default = LongArrayType(null)
            override fun byType(kType: KType): BundleSupportType<LongArray> =
                LongArrayType(kType.isMarkedNullable)
        }
    }

    class FloatType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<Float>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Float = 0f
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Float
        ) = bundle.putFloat(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Float?
        ): Float = bundle.getFloat(name, defaultValue ?: 0f)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Float?
        ): Float = intent.getFloatExtra(name, defaultValue ?: 0f)

        companion object : AutoFind.Creator<Float> {
            override val default = FloatType(null)
            override fun byType(kType: KType): BundleSupportType<Float> =
                FloatType(kType.isMarkedNullable)
        }
    }

    class FloatArrayType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<FloatArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): FloatArray = floatArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: FloatArray
        ) = bundle.putFloatArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): FloatArray? = bundle.getFloatArray(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): FloatArray? = intent.getFloatArrayExtra(name)

        companion object : AutoFind.Creator<FloatArray> {
            override val default = FloatArrayType(null)
            override fun byType(kType: KType): BundleSupportType<FloatArray> =
                FloatArrayType(kType.isMarkedNullable)
        }
    }

    class DoubleType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<Double>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Double = 0.0
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Double
        ) = bundle.putDouble(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Double?
        ): Double = bundle.getDouble(name, defaultValue ?: 0.0)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Double?
        ): Double = intent.getDoubleExtra(name, defaultValue ?: 0.0)

        companion object : AutoFind.Creator<Double> {
            override val default = DoubleType(null)
            override fun byType(kType: KType): BundleSupportType<Double> =
                DoubleType(kType.isMarkedNullable)
        }
    }

    class DoubleArrayType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<DoubleArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): DoubleArray = doubleArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: DoubleArray
        ) = bundle.putDoubleArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): DoubleArray? = bundle.getDoubleArray(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): DoubleArray? = intent.getDoubleArrayExtra(name)

        companion object : AutoFind.Creator<DoubleArray> {
            override val default = DoubleArrayType(null)
            override fun byType(kType: KType): BundleSupportType<DoubleArray> =
                DoubleArrayType(kType.isMarkedNullable)
        }
    }

    class BooleanType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<Boolean>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Boolean = false
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Boolean
        ) = bundle.putBoolean(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Boolean?
        ): Boolean = bundle.getBoolean(name, defaultValue == true)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Boolean?
        ): Boolean = intent.getBooleanExtra(name, defaultValue == true)

        companion object : AutoFind.Creator<Boolean> {
            override val default = BooleanType(null)
            override fun byType(kType: KType): BundleSupportType<Boolean> =
                BooleanType(kType.isMarkedNullable)
        }
    }

    class BooleanArrayType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<BooleanArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): BooleanArray = booleanArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: BooleanArray
        ) = bundle.putBooleanArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): BooleanArray? = bundle.getBooleanArray(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): BooleanArray? = intent.getBooleanArrayExtra(name)

        companion object : AutoFind.Creator<BooleanArray> {
            override val default = BooleanArrayType(null)
            override fun byType(kType: KType): BundleSupportType<BooleanArray> =
                BooleanArrayType(kType.isMarkedNullable)
        }
    }

    class CharType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<Char>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Char = ' '
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Char
        ) = bundle.putChar(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Char?
        ): Char = bundle.getChar(name, defaultValue ?: ' ')

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Char?
        ): Char = intent.getCharExtra(name, defaultValue ?: ' ')

        companion object : AutoFind.Creator<Char> {
            override val default = CharType(null)
            override fun byType(kType: KType): BundleSupportType<Char> =
                CharType(kType.isMarkedNullable)
        }
    }

    class CharArrayType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<CharArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): CharArray = charArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: CharArray
        ) = bundle.putCharArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): CharArray? = bundle.getCharArray(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): CharArray? = intent.getCharArrayExtra(name)

        companion object : AutoFind.Creator<CharArray> {
            override val default = CharArrayType(null)
            override fun byType(kType: KType): BundleSupportType<CharArray> =
                CharArrayType(kType.isMarkedNullable)
        }
    }
    //</editor-fold>

    //<editor-fold desc="原生的final类型" defaultstatus="collapsed">
    class StringType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<String>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): String = ""
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: String
        ) = bundle.putString(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): String? = bundle.getString(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): String? = intent.getStringExtra(name)

        companion object : AutoFind.Creator<String> {
            override val default = StringType(null)
            override fun byType(kType: KType): BundleSupportType<String> =
                StringType(kType.isMarkedNullable)
        }
    }

    class BundleType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<Bundle>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Bundle = Bundle.EMPTY
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Bundle
        ) = bundle.putBundle(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Bundle? = bundle.getBundle(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Bundle? = intent.getBundleExtra(name)

        companion object : AutoFind.Creator<Bundle> {
            override val default = BundleType(null)
            override fun byType(kType: KType): BundleSupportType<Bundle> =
                BundleType(kType.isMarkedNullable)
        }
    }

    /**
     * [Size] 类型，只有[Bundle]的读写，不支持[Intent]
     */
    class SizeType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<Size>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Size = Size(0, 0)
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Size
        ) = bundle.putSize(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Size? = bundle.getSize(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Size? = throw IllegalArgumentException("Not support return type: $property")

        companion object : AutoFind.Creator<Size> {
            override val default = SizeType(null)
            override fun byType(kType: KType): BundleSupportType<Size> =
                SizeType(kType.isMarkedNullable)
        }
    }

    /**
     * [SizeF] 类型，只有[Bundle]的读写，不支持[Intent]
     */
    class SizeFType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<SizeF>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): SizeF = SizeF(0f, 0f)
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: SizeF
        ) = bundle.putSizeF(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): SizeF? = bundle.getSizeF(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): SizeF? = throw IllegalArgumentException("Not support return type: $property")

        companion object : AutoFind.Creator<SizeF> {
            override val default = SizeFType(null)
            override fun byType(kType: KType): BundleSupportType<SizeF> =
                SizeFType(kType.isMarkedNullable)
        }
    }
    //</editor-fold>

    //<editor-fold desc="原生的非final类型" defaultstatus="collapsed">
    @Suppress("kotlin:S6530", "UNCHECKED_CAST")
    class ParcelableType<T : Parcelable>(
        private val clazz: Class<T>?,
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<T>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): T =
            (clazz ?: (property.returnType.classifier as KClass<T>).java).newInstance()

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: T
        ) = bundle.putParcelable(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): T? = BundleCompat.getParcelable(
            bundle, name, clazz ?: (property.returnType.classifier as KClass<T>).java
        )

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): T? = IntentCompat.getParcelableExtra(
            intent, name, clazz ?: (property.returnType.classifier as KClass<T>).java
        )

        companion object : AutoFind.Creator<Parcelable> {
            override val default = ParcelableType<Parcelable>(null)
            override fun byType(kType: KType): BundleSupportType<Parcelable> =
                ParcelableType(
                    (kType.classifier as KClass<Parcelable>).java,
                    kType.isMarkedNullable
                )

            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified T : Parcelable> invoke(
                isMarkedNullable: Boolean = false
            ) = ParcelableType(T::class.java, isMarkedNullable)
        }
    }

    class CharSequenceType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<CharSequence>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): CharSequence = ""
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: CharSequence
        ) = bundle.putCharSequence(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): CharSequence? = bundle.getCharSequence(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): CharSequence? = intent.getCharSequenceExtra(name)

        companion object : AutoFind.Creator<CharSequence> {
            override val default = CharSequenceType(null)
            override fun byType(kType: KType): BundleSupportType<CharSequence> =
                CharSequenceType(kType.isMarkedNullable)
        }
    }

    @Suppress("kotlin:S6530", "UNCHECKED_CAST")
    class SparseArrayType<T : Parcelable>(
        private val clazz: Class<T>?,
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<SparseArray<T>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): SparseArray<T> = SparseArray()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: SparseArray<T>
        ) = bundle.putSparseParcelableArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): SparseArray<T>? = BundleCompat.getSparseParcelableArray(
            bundle, name, clazz ?: property.returnType.argument0TypeClass.java as Class<T>
        )

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): SparseArray<T>? = throw IllegalArgumentException("Not support return type: $property")

        companion object : AutoFind.Creator<SparseArray<Parcelable>> {
            override val default = SparseArrayType<Parcelable>(null, null)
            override fun byType(kType: KType): BundleSupportType<SparseArray<Parcelable>> =
                SparseArrayType(
                    kType.argument0TypeClass.java as Class<Parcelable>,
                    kType.isMarkedNullable
                )

            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified T : Parcelable> invoke(
                isMarkedNullable: Boolean = false
            ) = SparseArrayType(T::class.java, isMarkedNullable)
        }
    }

    class IBinderType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<IBinder>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): IBinder {
            TODO("Not yet implemented")
        }

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: IBinder
        ) = bundle.putBinder(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): IBinder? = bundle.getBinder(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): IBinder? = throw IllegalArgumentException("Not support return type: $property")

        companion object : AutoFind.Creator<IBinder> {
            override val default = IBinderType(null)
            override fun byType(kType: KType): BundleSupportType<IBinder> =
                IBinderType(kType.isMarkedNullable)
        }
    }

    @Suppress("kotlin:S6530", "UNCHECKED_CAST")
    class SerializableType<T : Serializable>(
        private val clazz: Class<T>?,
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<T>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): T =
            (clazz ?: (property.returnType.classifier as KClass<T>).java).newInstance()

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: T
        ) = bundle.putSerializable(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): T? = BundleCompat.getSerializable(
            bundle, name, clazz ?: (property.returnType.classifier as KClass<T>).java
        )

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): T? = IntentCompat.getSerializableExtra(
            intent, name, clazz ?: (property.returnType.classifier as KClass<T>).java
        )

        companion object : AutoFind.Creator<Serializable> {
            override val default = SerializableType<Serializable>(null, null)
            override fun byType(kType: KType): BundleSupportType<Serializable> =
                SerializableType(
                    (kType.classifier as KClass<Serializable>).java,
                    kType.isMarkedNullable
                )

            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified T : Serializable> invoke(
                isMarkedNullable: Boolean = false
            ) = SerializableType(T::class.java, isMarkedNullable)
        }
    }
    //</editor-fold>

    //<editor-fold desc="List的类型" defaultstatus="collapsed">
    @Suppress("kotlin:S6530", "UNCHECKED_CAST")
    class ListParcelableType<T : Parcelable>(
        private val clazz: Class<T>?,
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<List<T>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): List<T> = emptyList()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: List<T>
        ) = bundle.putParcelableArrayList(name, value.asArrayList())

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): List<T>? = BundleCompat.getParcelableArrayList(
            bundle, name, clazz ?: property.returnType.argument0TypeClass.java as Class<T>
        )

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): List<T>? = IntentCompat.getParcelableArrayListExtra(
            intent, name, clazz ?: property.returnType.argument0TypeClass.java as Class<T>
        )

        companion object : AutoFind.Creator<List<Parcelable>> {
            override val default = ListParcelableType<Parcelable>(null, null)
            override fun byType(kType: KType): BundleSupportType<List<Parcelable>> =
                ListParcelableType(
                    kType.argument0TypeClass.java as Class<Parcelable>,
                    kType.isMarkedNullable
                )

            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified T : Parcelable> invoke(
                isMarkedNullable: Boolean = false
            ) = ListParcelableType(T::class.java, isMarkedNullable)
        }
    }

    class ListStringType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<List<String>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): List<String> = emptyList()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: List<String>
        ) = bundle.putStringArrayList(name, value.asArrayList())

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): List<String>? = bundle.getStringArrayList(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): List<String>? = intent.getStringArrayListExtra(name)

        companion object : AutoFind.Creator<List<String>> {
            override val default = ListStringType(null)
            override fun byType(kType: KType): BundleSupportType<List<String>> =
                ListStringType(kType.isMarkedNullable)
        }
    }

    @Suppress("REDUNDANT_PROJECTION")
    class ListCharSequenceType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<List<out CharSequence>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): List<out CharSequence> = emptyList()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: List<out CharSequence>
        ) = bundle.putCharSequenceArrayList(name, value.asArrayList())

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): List<out CharSequence>? = bundle.getCharSequenceArrayList(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): List<out CharSequence>? = intent.getCharSequenceArrayListExtra(name)

        companion object : AutoFind.Creator<List<out CharSequence>> {
            override val default = ListCharSequenceType(null)
            override fun byType(kType: KType): BundleSupportType<List<out CharSequence>> =
                ListCharSequenceType(kType.isMarkedNullable)
        }
    }

    class ListIntegerType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<List<Int>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): List<Int> = emptyList()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: List<Int>
        ) = bundle.putIntegerArrayList(name, value.asArrayList())

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): List<Int>? = bundle.getIntegerArrayList(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): List<Int>? = intent.getIntegerArrayListExtra(name)

        companion object : AutoFind.Creator<List<Int>> {
            override val default = ListIntegerType(null)
            override fun byType(kType: KType): BundleSupportType<List<Int>> =
                ListIntegerType(kType.isMarkedNullable)
        }
    }
    //</editor-fold>

    //<editor-fold desc="类类型的数组" defaultstatus="collapsed">
    @Suppress("kotlin:S6530", "UNCHECKED_CAST")
    class ArrayParcelableType<T : Parcelable>(
        private val clazz: Class<T>?,
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<Array<T>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Array<T> =
            (clazz ?: property.returnType.argument0TypeClass.java as Class<T>)
                .PARCELABLE_CREATOR.newArray(0)

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Array<T>
        ) = bundle.putParcelableArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Array<T>? {
            val clazz = clazz ?: property.returnType.argument0TypeClass.java as Class<T>
            val array = BundleCompat.getParcelableArray(bundle, name, clazz)
                ?: return null
            val out = clazz.PARCELABLE_CREATOR.newArray(array.size)
            System.arraycopy(array, 0, out, 0, array.size)
            return out
        }

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Array<T>? {
            val clazz = clazz ?: property.returnType.argument0TypeClass.java as Class<T>
            val array = IntentCompat.getParcelableArrayExtra(intent, name, clazz)
                ?: return null
            val out = clazz.PARCELABLE_CREATOR.newArray(array.size)
            System.arraycopy(array, 0, out, 0, array.size)
            return out
        }

        companion object : AutoFind.Creator<Array<Parcelable>> {
            override val default = ArrayParcelableType<Parcelable>(null, null)
            override fun byType(kType: KType): BundleSupportType<Array<Parcelable>> =
                ArrayParcelableType(
                    kType.argument0TypeClass.java as Class<Parcelable>,
                    kType.isMarkedNullable
                )

            //            inline fun <reified L : Array<T>, reified T : Parcelable> ArrayParcelableType() =
//                ArrayParcelableType(T::class.java, typeOf<L>().isMarkedNullable)
            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified T : Parcelable> invoke(
                isMarkedNullable: Boolean = false
            ) = ArrayParcelableType(T::class.java, isMarkedNullable)
        }
    }

    class ArrayStringType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<Array<String>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Array<String> = arrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Array<String>
        ) = bundle.putStringArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Array<String>? = bundle.getStringArray(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Array<String>? = intent.getStringArrayExtra(name)

        companion object : AutoFind.Creator<Array<String>> {
            override val default = ArrayStringType(null)
            override fun byType(kType: KType): BundleSupportType<Array<String>> =
                ArrayStringType(kType.isMarkedNullable)
        }
    }

    class ArrayCharSequenceType(
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<Array<CharSequence>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Array<CharSequence> = arrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Array<CharSequence>
        ) = bundle.putCharSequenceArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Array<CharSequence>? = bundle.getCharSequenceArray(name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Array<CharSequence>? = intent.getCharSequenceArrayExtra(name)

        companion object : AutoFind.Creator<Array<CharSequence>> {
            override val default = ArrayCharSequenceType(null)
            override fun byType(kType: KType): BundleSupportType<Array<CharSequence>> =
                ArrayCharSequenceType(kType.isMarkedNullable)
        }
    }
    //</editor-fold>

    //<editor-fold desc="扩展支持" defaultstatus="collapsed">
    /**
     * 枚举类型，存储时存放的是它的[Enum.name]
     */
    @Suppress("kotlin:S6530", "UNCHECKED_CAST")
    class EnumType<T : Enum<T>>(
        private val values: Array<T>?,
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<T>(isMarkedNullable) {
        constructor(
            clazz: Class<T>, isMarkedNullable: Boolean?
        ) : this(clazz.enumConstants, isMarkedNullable)

        override fun nonnullValue(property: KProperty<*>): T =
            (values ?: (property.returnType.classifier as KClass<T>).java.enumConstants).first()

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: T
        ) = bundle.putString(name, value.name)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): T? = forName(property, bundle.getString(name)!!)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): T? = forName(property, intent.getStringExtra(name)!!)

        private fun forName(
            property: KProperty<*>, name: String
        ): T? = values?.find { it.name == name } ?: java.lang.Enum.valueOf(
            (property.returnType.classifier as KClass<T>).java, name
        )

        companion object : AutoFind.Creator<Enum<*>> {
            override val default = EnumType(null, null) as BundleSupportType<Enum<*>>
            override fun byType(kType: KType): BundleSupportType<Enum<*>> =
                EnumType(
                    (kType.classifier as KClass<out Enum<*>>).java.enumConstants,
                    kType.isMarkedNullable
                ) as BundleSupportType<Enum<*>>

            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified E : Enum<E>> invoke(
                isMarkedNullable: Boolean = false
            ) = EnumType<E>(enumValues<E>(), isMarkedNullable)
        }
    }

    /**
     * Protobuf 类型，存储的是它的[MessageLite.toByteArray]
     *
     * @param writeClassName 如果 [parser] 为空，是否需要在序列化时同时序列化类名；
     * 如果[parser] 不为null，则无效
     */
    class ProtoBufType<T : MessageLite>(
        private val parser: Parser<T>?,
        private val writeClassName: Boolean,
        isMarkedNullable: Boolean? = null
    ) : BundleSupportType<T>(isMarkedNullable) {
        // Protobuf生成的类都是final的，如果是最终生成的实体类则不需要存储类名
        constructor(
            clazz: Class<T>, isMarkedNullable: Boolean?
        ) : this(findParser(clazz), Modifier.FINAL !in clazz.modifiers, isMarkedNullable)

        override fun nonnullValue(property: KProperty<*>): T =
            parseData(property, byteArrayOf())!!

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: T
        ) {
            val byteArray = if (parser == null && writeClassName) {
                val className = value.javaClass.name.toByteArray()
                className.size.toByteArray() + className + value.toByteArray()
            } else {
                value.toByteArray()
            }
            bundle.putByteArray(name, byteArray)
        }

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): T? = parseData(property, bundle.getByteArray(name))

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): T? = parseData(property, intent.getByteArrayExtra(name))

        @Suppress("kotlin:S6530", "UNCHECKED_CAST")
        private fun parseData(
            property: KProperty<*>, data: ByteArray?
        ): T? = if (data == null) {
            null
        } else if (!writeClassName) {
            val parser = parser ?: run {
                val clazz = (property.returnType.classifier as KClass<T>).java
                findParser(clazz)
            } ?: throw IllegalArgumentException(
                "对于没有传入 parser 且字段类型未精确到实体类的Protobuf字段委托，需要设置 writeClassName 为true"
            )
            parser.parseFrom(data)
        } else DataInputStream(ByteArrayInputStream(data)).use { input ->
            val size = input.readInt()
            val className = String(input.readNBytesCompat(size))
            findParser(Class.forName(className) as Class<T>)!!.parseFrom(input)
        }

        private fun DataInputStream.readNBytesCompat(
            size: Int
        ): ByteArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readNBytes(size)
        } else {
            val bytes = ByteArray(size)
            var n = 0
            while (n < size) {
                val count = read(bytes, n, size - n)
                if (count < 0)
                    break
                n += count
            }
            read(bytes, 0, size)
            bytes
        }

        companion object : AutoFind.Creator<MessageLite> {
            override val default = ProtoBufType<MessageLite>(null, true, null)
            override fun byType(kType: KType): BundleSupportType<MessageLite> =
                ProtoBufType(
                    (kType.classifier as KClass<*>).java as Class<MessageLite>,
                    kType.isMarkedNullable
                )

            @Suppress("kotlin:S6531", "kotlin:S6530", "UNCHECKED_CAST")
            private fun <T : MessageLite> findParser(clazz: Class<T>): Parser<T>? {
                return if (Modifier.FINAL !in clazz.modifiers) {
                    null
                } else if (clazz.isAssignableFrom(GeneratedMessageV3::class.java)) {
                    (clazz as Class<out GeneratedMessageV3>)
                        .getProtobufV3DefaultInstance()
                        .parserForType as Parser<T>
                } else {
                    (clazz as Class<out GeneratedMessageLite<*, *>>)
                        .getProtobufLiteDefaultInstance()
                        .parserForType as Parser<T>
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="对外暴露自动寻找类型接口" defaultstatus="collapsed>
    class NotSupportType : BundleSupportType<Any>(false) {
        override fun nonnullValue(property: KProperty<*>): Any {
            throw IllegalAccessException()
        }

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Any
        ) = throw IllegalArgumentException("Not support return type: $property")

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Any? = throw IllegalArgumentException("Not support return type: $property")

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Any?
        ): Any = throw IllegalArgumentException("Not support return type: $property")

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Any? = throw IllegalArgumentException("Not support return type: $property")

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Any?
        ): Any = throw IllegalArgumentException("Not support return type: $property")

        companion object : AutoFind.Creator<Any> {
            override val default = NotSupportType()
            override fun byType(kType: KType): BundleSupportType<Any> =
                default
        }
    }

    /**
     * 通过反射字段类型来获取其访问器
     */
    object AutoFind : BundleSupportType<Any>(null) {
        override fun nonnullValue(property: KProperty<*>): Any {
            throw IllegalAccessException()
        }

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Any
        ) = findType(property).putNonnull(bundle, property, name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Any? = findType(property).getNullable(bundle, property, name)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Any?
        ): Any = findType(property).getNonnull(bundle, property, name, defaultValue)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Any? = findType(property).getExtraNullable(intent, property, name)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Any?
        ): Any = findType(property).getExtraNonnull(intent, property, name, defaultValue)

        private fun findType(property: KProperty<*>): BundleSupportType<Any> {
            return findByType(property.returnType, false)
        }

        inline fun <reified T> findByType(): BundleSupportType<T> =
            findByType(typeOf<T>(), true)

        @Suppress("CyclomaticComplexMethod")
        fun <T> findByType(
            kType: KType, checkNullable: Boolean
        ): BundleSupportType<T> = Debug.countTime(TAG, "findByType") {
            val classifier = kType.classifier
            val type: Creator<*> = if (classifier !is KClass<*>) {
                NotSupportType
            } else {
                val clazz = classifier.java
                when {
                    // 基础数据类型与其数组
                    clazz == java.lang.Byte::class.java || clazz == java.lang.Byte.TYPE -> ByteType
                    clazz == ByteArray::class.java -> ByteArrayType
                    clazz == java.lang.Short::class.java || clazz == java.lang.Short.TYPE -> ShortType
                    clazz == ShortArray::class.java -> ShortArrayType
                    clazz == java.lang.Integer::class.java || clazz == java.lang.Integer.TYPE -> IntType
                    clazz == IntArray::class.java -> IntArrayType
                    clazz == java.lang.Long::class.java || clazz == java.lang.Long.TYPE -> LongType
                    clazz == LongArray::class.java -> LongArrayType
                    clazz == java.lang.Float::class.java || clazz == java.lang.Float.TYPE -> FloatType
                    clazz == FloatArray::class.java -> FloatArrayType
                    clazz == java.lang.Double::class.java || clazz == java.lang.Double.TYPE -> DoubleType
                    clazz == DoubleArray::class.java -> DoubleArrayType
                    clazz == java.lang.Boolean::class.java || clazz == java.lang.Boolean.TYPE -> BooleanType
                    clazz == BooleanArray::class.java -> BooleanArrayType
                    clazz == java.lang.Character::class.java || clazz == java.lang.Character.TYPE -> CharType
                    clazz == CharArray::class.java -> CharArrayType
                    // 原生的final类型
                    clazz == String::class.java -> StringType
                    clazz == Bundle::class.java -> BundleType
                    clazz == Size::class.java -> SizeType
                    clazz == SizeF::class.java -> SizeFType
                    // 原生的非final类型
                    clazz.isSubclassOf(Parcelable::class.java) -> ParcelableType
                    clazz.isSubclassOf(CharSequence::class.java) -> CharSequenceType
                    clazz.isSubclassOf(SparseArray::class.java) -> SparseArrayType
                    clazz.isSubclassOf(IBinder::class.java) -> IBinderType
                    // List类型
                    clazz.isSubclassOf(List::class.java) -> {
                        val arg0Class = kType.argument0TypeClass.java
                        when {
                            arg0Class.isSubclassOf(Parcelable::class.java) -> ListParcelableType
                            arg0Class == String::class.java -> ListStringType
                            arg0Class.isSubclassOf(CharSequence::class.java) -> ListCharSequenceType
                            arg0Class == Integer::class.java -> ListIntegerType
                            else -> NotSupportType
                        }
                    }
                    // 数组类型
                    clazz.isSubclassOf(Array<Parcelable>::class.java) -> ArrayParcelableType
                    clazz == Array<String>::class.java -> ArrayStringType
                    clazz.isSubclassOf(Array<CharSequence>::class.java) -> ArrayCharSequenceType
                    // 扩展支持
                    clazz.isSubclassOf(Enum::class.java) -> EnumType
                    DependencyChecker.PROTOBUF_LITE() && clazz.isSubclassOf(MessageLite::class.java) ->
                        ProtoBufType
                    // 原生的非final类型（protobuf 标准版的实体类都实现了 Serializable 接口，避免使用其为protobuf序列化）
                    clazz.isSubclassOf(Serializable::class.java) -> SerializableType
                    else -> NotSupportType
                }
            }
            @Suppress("UNCHECKED_CAST")
            return if (checkNullable) type.byType(kType) as BundleSupportType<T>
            else type.default as BundleSupportType<T>
        }

        interface Creator<T> {
            val default: BundleSupportType<T>
            fun byType(kType: KType): BundleSupportType<T>
        }
    }
    //</editor-fold>

    companion object {
        private const val TAG = "BundleSupportType"
        val KType.argument0TypeClass: KClass<*>
            get() = arguments[0].type?.classifier as KClass<*>
    }
}
