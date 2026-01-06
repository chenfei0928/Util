package io.github.chenfei0928.os

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.util.Size
import android.util.SizeF
import android.util.SparseArray
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import androidx.core.os.ParcelCompat
import androidx.core.util.size
import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import com.google.protobuf.ProtobufListParceler
import com.google.protobuf.protobufParserForType
import io.github.chenfei0928.collection.asArrayList
import io.github.chenfei0928.io.readNBytesCompat
import io.github.chenfei0928.lang.contains
import io.github.chenfei0928.lang.toByteArray
import io.github.chenfei0928.os.BundleSupportType.Companion.getReturnTypeJClass
import io.github.chenfei0928.reflect.LazyTypeToken
import io.github.chenfei0928.reflect.isSubclassOf
import io.github.chenfei0928.reflect.jvmErasureClassOrNull
import io.github.chenfei0928.util.DependencyChecker
import kotlinx.parcelize.Parceler
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.Serializable
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

/**
 * 子类实现要求重写[nonnullValue]提供默认值，重写[putNonnull]来对[Bundle]进行写入数据，
 * 并根据获取值get方法获取数据返回值的可空性重写[getNonnull]、[getExtraNonnull]
 * 或[getNullable]、[getExtraNullable]来返回数据。
 *
 * @param isMarkedNullable 标记是否可空，如果为null则根据[KProperty]的可空性判断
 *
 * @author chenf()
 * @date 2024-12-05 11:01
 */
@Suppress("unused", "TooManyFunctions")
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
        bundle: Bundle?, property: KProperty<*>, name: String, defaultValue: T?
    ): T? = if (isMarkedNullable ?: property.returnType.isMarkedNullable)
        bundle?.let { getNullable(bundle, property, name) }
    else if (bundle != null)
        getNonnull(bundle, property, name, defaultValue)
    else defaultValue ?: run {
        Log.d(TAG, "getValue: $name 没有找到数据，也未提供默认值，创建nonnullValue: $property")
        nonnullValue(property)
    }

    /**
     * 获取[Intent]的扩展数据，并返回可空数据。如果数据中没有存储该数据，则返回null
     */
    protected open fun getNullable(bundle: Bundle, property: KProperty<*>, name: String): T? =
        if (!bundle.containsKey(name)) null else getNonnull(bundle, property, name, null)

    /**
     * 获取[Intent]的扩展数据，并返回非空数据。如果数据中没有存储该数据，则返回默认数据
     */
    protected open fun getNonnull(
        bundle: Bundle, property: KProperty<*>, name: String, defaultValue: T?
    ): T = (if (!bundle.containsKey(name)) null else getNullable(bundle, property, name))
        ?: defaultValue ?: run {
            Log.d(TAG, "getNonnull: $name 没有找到数据，也未提供默认值，创建nonnullValue: $property")
            nonnullValue(property)
        }
    //</editor-fold>

    //<editor-fold desc="Intent的put" defaultstatus="collapsed">
    /**
     * 填充数据，如果[value]是null，将数据直接[Intent.removeExtra]，否则调用[putExtraNonnull]存放数据
     */
    fun putExtraNullable(
        intent: Intent, property: KProperty<*>, name: String, value: T?
    ): Intent = if (value == null) {
        intent.removeExtra(name)
        intent
    } else putExtraNonnull(intent, property, name, value)

    protected abstract fun putExtraNonnull(
        intent: Intent, property: KProperty<*>, name: String, value: T & Any
    ): Intent
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
    ): T? = if (isMarkedNullable ?: property.returnType.isMarkedNullable)
        getExtraNullable(intent, property, name)
    else getExtraNonnull(intent, property, name, defaultValue)

    /**
     * 获取[Intent]的扩展数据，并返回可空数据。如果数据中没有存储该数据，则返回null
     */
    protected open fun getExtraNullable(intent: Intent, property: KProperty<*>, name: String): T? =
        if (!intent.hasExtra(name)) null else getExtraNonnull(intent, property, name, null)

    /**
     * 获取[Intent]的扩展数据，并返回非空数据。如果数据中没有存储该数据，则返回默认数据
     */
    protected open fun getExtraNonnull(
        intent: Intent, property: KProperty<*>, name: String, defaultValue: T?
    ): T = (if (!intent.hasExtra(name)) null else getExtraNullable(intent, property, name))
        ?: defaultValue ?: run {
            Log.d(TAG, run {
                "getExtraNonnull: $name 没有找到数据，也未提供默认值，创建nonnullValue: $property"
            })
            nonnullValue(property)
        }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="基础数据类型与其数组" defaultstatus="collapsed">
    //<editor-fold desc="整型与其数组" defaultstatus="collapsed">
    class ByteType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<Byte>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Byte = 0
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Byte
        ) = bundle.putByte(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Byte?
        ): Byte = bundle.getByte(name, 0)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Byte
        ) = intent.putExtra(name, value)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Byte?
        ): Byte = intent.getByteExtra(name, 0)

        companion object : AutoFind.Creator<Byte>() {
            override val checkByReflectWhenCall = ByteType(null)
            override val commonCase = ByteType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Byte> = ByteType(isMarkedNullable)
        }
    }

    class ByteArrayType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<ByteArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): ByteArray = byteArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: ByteArray
        ) = bundle.putByteArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): ByteArray? = bundle.getByteArray(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: ByteArray
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): ByteArray? = intent.getByteArrayExtra(name)

        companion object : AutoFind.Creator<ByteArray>() {
            override val checkByReflectWhenCall = ByteArrayType(null)
            override val commonCase = ByteArrayType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<ByteArray> = ByteArrayType(isMarkedNullable)
        }
    }

    class ShortType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<Short>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Short = 0
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Short
        ) = bundle.putShort(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Short?
        ): Short = bundle.getShort(name, defaultValue ?: 0)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Short
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Short?
        ): Short = intent.getShortExtra(name, defaultValue ?: 0)

        companion object : AutoFind.Creator<Short>() {
            override val checkByReflectWhenCall = ShortType(null)
            override val commonCase = ShortType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Short> = ShortType(isMarkedNullable)
        }
    }

    class ShortArrayType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<ShortArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): ShortArray = shortArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: ShortArray
        ) = bundle.putShortArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): ShortArray? = bundle.getShortArray(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: ShortArray
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): ShortArray? = intent.getShortArrayExtra(name)

        companion object : AutoFind.Creator<ShortArray>() {
            override val checkByReflectWhenCall = ShortArrayType(null)
            override val commonCase = ShortArrayType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<ShortArray> = ShortArrayType(isMarkedNullable)
        }
    }

    class IntType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<Int>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Int = 0
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Int
        ) = bundle.putInt(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Int?
        ): Int = bundle.getInt(name, defaultValue ?: 0)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Int
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Int?
        ): Int = intent.getIntExtra(name, defaultValue ?: 0)

        companion object : AutoFind.Creator<Int>() {
            override val checkByReflectWhenCall = IntType(null)
            override val commonCase = IntType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Int> = IntType(isMarkedNullable)
        }
    }

    class IntArrayType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<IntArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): IntArray = intArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: IntArray
        ) = bundle.putIntArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): IntArray? = bundle.getIntArray(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: IntArray
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): IntArray? = intent.getIntArrayExtra(name)

        companion object : AutoFind.Creator<IntArray>() {
            override val checkByReflectWhenCall = IntArrayType(null)
            override val commonCase = IntArrayType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<IntArray> = IntArrayType(isMarkedNullable)
        }
    }

    class LongType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<Long>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Long = 0
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Long
        ) = bundle.putLong(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Long?
        ): Long = bundle.getLong(name, 0)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Long
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Long?
        ): Long = intent.getLongExtra(name, 0)

        companion object : AutoFind.Creator<Long>() {
            override val checkByReflectWhenCall = LongType(null)
            override val commonCase = LongType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Long> = LongType(isMarkedNullable)
        }
    }

    class LongArrayType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<LongArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): LongArray = longArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: LongArray
        ) = bundle.putLongArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): LongArray? = bundle.getLongArray(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: LongArray
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): LongArray? = intent.getLongArrayExtra(name)

        companion object : AutoFind.Creator<LongArray>() {
            override val checkByReflectWhenCall = LongArrayType(null)
            override val commonCase = LongArrayType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<LongArray> = LongArrayType(isMarkedNullable)
        }
    }
    //</editor-fold>

    //<editor-fold desc="浮点型" defaultstatus="collapsed">
    class FloatType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<Float>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Float = 0f
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Float
        ) = bundle.putFloat(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Float?
        ): Float = bundle.getFloat(name, defaultValue ?: 0f)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Float
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Float?
        ): Float = intent.getFloatExtra(name, defaultValue ?: 0f)

        companion object : AutoFind.Creator<Float>() {
            override val checkByReflectWhenCall = FloatType(null)
            override val commonCase = FloatType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Float> = FloatType(isMarkedNullable)
        }
    }

    class FloatArrayType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<FloatArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): FloatArray = floatArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: FloatArray
        ) = bundle.putFloatArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): FloatArray? = bundle.getFloatArray(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: FloatArray
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): FloatArray? = intent.getFloatArrayExtra(name)

        companion object : AutoFind.Creator<FloatArray>() {
            override val checkByReflectWhenCall = FloatArrayType(null)
            override val commonCase = FloatArrayType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<FloatArray> = FloatArrayType(isMarkedNullable)
        }
    }

    class DoubleType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<Double>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Double = 0.0
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Double
        ) = bundle.putDouble(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Double?
        ): Double = bundle.getDouble(name, defaultValue ?: 0.0)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Double
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Double?
        ): Double = intent.getDoubleExtra(name, defaultValue ?: 0.0)

        companion object : AutoFind.Creator<Double>() {
            override val checkByReflectWhenCall = DoubleType(null)
            override val commonCase = DoubleType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Double> = DoubleType(isMarkedNullable)
        }
    }

    class DoubleArrayType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<DoubleArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): DoubleArray = doubleArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: DoubleArray
        ) = bundle.putDoubleArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): DoubleArray? = bundle.getDoubleArray(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: DoubleArray
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): DoubleArray? = intent.getDoubleArrayExtra(name)

        companion object : AutoFind.Creator<DoubleArray>() {
            override val checkByReflectWhenCall = DoubleArrayType(null)
            override val commonCase = DoubleArrayType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<DoubleArray> = DoubleArrayType(isMarkedNullable)
        }
    }
    //</editor-fold>

    class BooleanType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<Boolean>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Boolean = false
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Boolean
        ) = bundle.putBoolean(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Boolean?
        ): Boolean = bundle.getBoolean(name, defaultValue == true)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Boolean
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Boolean?
        ): Boolean = intent.getBooleanExtra(name, defaultValue == true)

        companion object : AutoFind.Creator<Boolean>() {
            override val checkByReflectWhenCall = BooleanType(null)
            override val commonCase = BooleanType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Boolean> = BooleanType(isMarkedNullable)
        }
    }

    class BooleanArrayType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<BooleanArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): BooleanArray = booleanArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: BooleanArray
        ) = bundle.putBooleanArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): BooleanArray? = bundle.getBooleanArray(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: BooleanArray
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): BooleanArray? = intent.getBooleanArrayExtra(name)

        companion object : AutoFind.Creator<BooleanArray>() {
            override val checkByReflectWhenCall = BooleanArrayType(null)
            override val commonCase = BooleanArrayType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<BooleanArray> = BooleanArrayType(isMarkedNullable)
        }
    }

    class CharType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<Char>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Char = ' '
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Char
        ) = bundle.putChar(name, value)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Char?
        ): Char = bundle.getChar(name, defaultValue ?: ' ')

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Char
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Char?
        ): Char = intent.getCharExtra(name, defaultValue ?: ' ')

        companion object : AutoFind.Creator<Char>() {
            override val checkByReflectWhenCall = CharType(null)
            override val commonCase = CharType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Char> = CharType(isMarkedNullable)
        }
    }

    class CharArrayType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<CharArray>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): CharArray = charArrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: CharArray
        ) = bundle.putCharArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): CharArray? = bundle.getCharArray(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: CharArray
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): CharArray? = intent.getCharArrayExtra(name)

        companion object : AutoFind.Creator<CharArray>() {
            override val checkByReflectWhenCall = CharArrayType(null)
            override val commonCase = CharArrayType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<CharArray> = CharArrayType(isMarkedNullable)
        }
    }
    //</editor-fold>

    //<editor-fold desc="原生的final类型" defaultstatus="collapsed">
    class StringType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<String>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): String = ""
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: String
        ) = bundle.putString(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): String? = bundle.getString(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: String
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): String? = intent.getStringExtra(name)

        companion object : AutoFind.Creator<String>() {
            override val checkByReflectWhenCall = StringType(null)
            override val commonCase = StringType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<String> = StringType(isMarkedNullable)
        }
    }

    class BundleType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<Bundle>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Bundle = Bundle.EMPTY
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Bundle
        ) = bundle.putBundle(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Bundle? = bundle.getBundle(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Bundle
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Bundle? = intent.getBundleExtra(name)

        companion object : AutoFind.Creator<Bundle>() {
            override val checkByReflectWhenCall = BundleType(null)
            override val commonCase = BundleType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Bundle> = BundleType(isMarkedNullable)
        }
    }

    /**
     * [Size] 类型，只有[Bundle]的读写，不支持[Intent]
     */
    class SizeType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<Size>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Size = Size(0, 0)
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Size
        ) = bundle.putSize(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Size? = bundle.getSize(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Size
        ): Intent = intent.putExtra(name, ParcelUtil.marshall(value, parceler))

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Size? = intent.getByteArrayExtra(name)?.let { ParcelUtil.unmarshall(it, parceler) }

        companion object : AutoFind.Creator<Size>() {
            override val checkByReflectWhenCall = SizeType(null)
            override val commonCase = SizeType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Size> = SizeType(isMarkedNullable)

            private val parceler = object : Parceler<Size> {
                override fun create(parcel: Parcel): Size = Size(parcel.readInt(), parcel.readInt())
                override fun Size.write(parcel: Parcel, flags: Int) {
                    parcel.writeInt(width)
                    parcel.writeInt(height)
                }
            }
        }
    }

    /**
     * [SizeF] 类型，只有[Bundle]的读写，不支持[Intent]
     */
    class SizeFType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<SizeF>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): SizeF = SizeF(0f, 0f)
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: SizeF
        ) = bundle.putSizeF(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): SizeF? = bundle.getSizeF(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: SizeF
        ): Intent = intent.putExtra(name, ParcelUtil.marshall(value, parceler))

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): SizeF? = intent.getByteArrayExtra(name)?.let { ParcelUtil.unmarshall(it, parceler) }

        companion object : AutoFind.Creator<SizeF>() {
            override val checkByReflectWhenCall = SizeFType(null)
            override val commonCase = SizeFType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<SizeF> = SizeFType(isMarkedNullable)

            private val parceler = object : Parceler<SizeF> {
                override fun create(parcel: Parcel): SizeF =
                    SizeF(parcel.readFloat(), parcel.readFloat())

                override fun SizeF.write(parcel: Parcel, flags: Int) {
                    parcel.writeFloat(width)
                    parcel.writeFloat(height)
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="原生的非final类型" defaultstatus="collapsed">
    class ParcelableType<T : Parcelable?>(
        private val clazz: Class<T & Any>?,
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<T>(isMarkedNullable) {
        @Suppress("UNCHECKED_CAST")
        override fun nonnullValue(property: KProperty<*>): T & Any =
            clazz?.getDeclaredConstructor()?.newInstance()
                ?: property.returnType.jvmErasure.createInstance() as (T & Any)

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: T & Any
        ) = bundle.putParcelable(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): T? = BundleCompat.getParcelable(
            bundle, name, clazz ?: property.getReturnTypeJClass()
        )

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: T & Any
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): T? = IntentCompat.getParcelableExtra(
            intent, name, clazz ?: property.getReturnTypeJClass()
        )

        companion object : AutoFind.Creator<Parcelable>() {
            override val checkByReflectWhenCall = ParcelableType<Parcelable>(null)
            override val commonCase = ParcelableType<Parcelable>(null, false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Parcelable> = ParcelableType(
                type.tClass(), isMarkedNullable
            )

            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified T : Parcelable> invoke() =
                ParcelableType(T::class.java, false)

            inline fun <reified T : Parcelable> nullable(): BundleSupportType<T?> =
                ParcelableType(T::class.java, true)
        }
    }

    class CharSequenceType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<CharSequence>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): CharSequence = ""
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: CharSequence
        ) = bundle.putCharSequence(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): CharSequence? = bundle.getCharSequence(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: CharSequence
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): CharSequence? = intent.getCharSequenceExtra(name)

        companion object : AutoFind.Creator<CharSequence>() {
            override val checkByReflectWhenCall = CharSequenceType(null)
            override val commonCase = CharSequenceType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<CharSequence> = CharSequenceType(isMarkedNullable)
        }
    }

    class SparseArrayType<T : Parcelable>(
        private val clazz: Class<T>?,
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<SparseArray<T>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): SparseArray<T> = SparseArray()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: SparseArray<T>
        ) = bundle.putSparseParcelableArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): SparseArray<T>? = BundleCompat.getSparseParcelableArray(
            bundle, name, clazz ?: property.returnType.argument0TypeJClass<T>()
        )

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: SparseArray<T>
        ): Intent = intent.putExtra(name, ParcelUtil.marshall(value, getParceler(property)))

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): SparseArray<T>? = intent.getByteArrayExtra(name)?.let {
            ParcelUtil.unmarshall(it, getParceler(property))
        }

        //<editor-fold desc="SparseArray的Parceler序列化支持" defaultstatus="collapsed">
        private fun getParceler(property: KProperty<*>): Parceler<SparseArray<T>> =
            parceler ?: SparseArrayParcelableParceler(property.returnType.argument0TypeJClass())

        private val parceler: SparseArrayParcelableParceler<T>? =
            clazz?.let { SparseArrayParcelableParceler(clazz) }

        private class SparseArrayParcelableParceler<T : Parcelable>(
            private val clazz: Class<T>,
        ) : Parceler<SparseArray<T>> {
            override fun SparseArray<T>.write(parcel: Parcel, flags: Int) {
                val size = size
                parcel.writeInt(size)
                for (i in 0 until size) {
                    parcel.writeInt(keyAt(i))
                    val item = valueAt(i)
                    if (item != null) {
                        ParcelCompat.writeBoolean(parcel, true)
                        parcel.writeParcelable(item, flags)
                    } else {
                        ParcelCompat.writeBoolean(parcel, false)
                    }
                }
            }

            override fun create(parcel: Parcel): SparseArray<T> {
                val size = parcel.readInt()
                val array = SparseArray<T>(size)
                for (i in 0 until size) {
                    val key = parcel.readInt()
                    val item: T? = if (ParcelCompat.readBoolean(parcel)) {
                        ParcelCompat.readParcelable(parcel, clazz.classLoader, clazz)
                    } else null
                    array.put(key, item)
                }
                return array
            }
        }
        //</editor-fold>

        companion object : AutoFind.Creator<SparseArray<Parcelable>>() {
            override val checkByReflectWhenCall = SparseArrayType<Parcelable>(null, null)
            override val commonCase = SparseArrayType<Parcelable>(null, false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<SparseArray<Parcelable>> = SparseArrayType(
                type.argument0TypeClass(), isMarkedNullable
            )

            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified T : Parcelable> invoke() =
                SparseArrayType(T::class.java, false)

            @Suppress("UNCHECKED_CAST")
            inline fun <reified T : Parcelable> nullable(): BundleSupportType<SparseArray<T>?> =
                SparseArrayType(T::class.java, true) as BundleSupportType<SparseArray<T>?>
        }
    }

    class IBinderType<T : IBinder>(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<T>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): T =
            throw IllegalAccessException()

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: T
        ) = bundle.putBinder(name, value)

        @Suppress("UNCHECKED_CAST")
        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): T? = bundle.getBinder(name) as T

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: T
        ): Intent = throw IllegalArgumentException("Not support return type: $property")

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): T? = throw IllegalArgumentException("Not support return type: $property")

        companion object : AutoFind.Creator<IBinder>() {
            override val checkByReflectWhenCall = IBinderType<IBinder>(null)
            override val commonCase = IBinderType<IBinder>(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<IBinder> = IBinderType(isMarkedNullable)
        }
    }

    class SerializableType<T : Serializable?>(
        private val clazz: Class<T & Any>?,
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<T>(isMarkedNullable) {
        @Suppress("UNCHECKED_CAST")
        override fun nonnullValue(property: KProperty<*>): T & Any =
            clazz?.getDeclaredConstructor()?.newInstance()
                ?: property.returnType.jvmErasure.createInstance() as (T & Any)

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: T & Any
        ) = bundle.putSerializable(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): T? = BundleCompat.getSerializable(
            bundle, name, clazz ?: property.getReturnTypeJClass()
        )

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: T & Any
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): T? = IntentCompat.getSerializableExtra(
            intent, name, clazz ?: property.getReturnTypeJClass()
        )

        companion object : AutoFind.Creator<Serializable>() {
            override val checkByReflectWhenCall = SerializableType<Serializable>(null, null)
            override val commonCase = SerializableType<Serializable>(null, false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Serializable> = SerializableType(
                type.tClass(), isMarkedNullable
            )

            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified T : Serializable> invoke() =
                SerializableType(T::class.java, false)

            inline fun <reified T : Serializable> nullable(): BundleSupportType<T?> =
                SerializableType(T::class.java, true)
        }
    }
    //</editor-fold>

    //<editor-fold desc="List的类型" defaultstatus="collapsed">
    class ListParcelableType<T : Parcelable>(
        private val clazz: Class<T>?,
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<List<T>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): List<T> = emptyList()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: List<T>
        ) = bundle.putParcelableArrayList(name, value.asArrayList())

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): List<T>? = BundleCompat.getParcelableArrayList(
            bundle, name, clazz ?: property.returnType.argument0TypeJClass<T>()
        )

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: List<T>
        ): Intent = intent.putExtra(name, value.asArrayList())

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): List<T>? = IntentCompat.getParcelableArrayListExtra(
            intent, name, clazz ?: property.returnType.argument0TypeJClass<T>()
        )

        companion object : AutoFind.Creator<List<Parcelable>>() {
            override val checkByReflectWhenCall = ListParcelableType<Parcelable>(null, null)
            override val commonCase = ListParcelableType<Parcelable>(null, false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<List<Parcelable>> = ListParcelableType(
                type.argument0TypeClass(), isMarkedNullable
            )

            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified T : Parcelable> invoke() =
                ListParcelableType(T::class.java, false)

            @Suppress("UNCHECKED_CAST", "kotlin:S6531")
            inline fun <reified T : Parcelable> nullable(): BundleSupportType<List<T>?> =
                ListParcelableType(T::class.java, true) as BundleSupportType<List<T>?>
        }
    }

    class ListStringType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<List<String>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): List<String> = emptyList()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: List<String>
        ) = bundle.putStringArrayList(name, value.asArrayList())

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): List<String>? = bundle.getStringArrayList(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: List<String>
        ): Intent = intent.putExtra(name, value.asArrayList())

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): List<String>? = intent.getStringArrayListExtra(name)

        companion object : AutoFind.Creator<List<String>>() {
            override val checkByReflectWhenCall = ListStringType(null)
            override val commonCase = ListStringType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<List<String>> = ListStringType(isMarkedNullable)
        }
    }

    @Suppress("REDUNDANT_PROJECTION", "UNCHECKED_CAST")
    class ListCharSequenceType<T : CharSequence>(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<List<T>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): List<T> = emptyList()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: List<T>
        ) = bundle.putCharSequenceArrayList(name, value.asArrayList())

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): List<T>? = bundle.getCharSequenceArrayList(name) as List<T>

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: List<T>
        ): Intent = intent.putExtra(name, value.asArrayList())

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): List<T>? = intent.getCharSequenceArrayListExtra(name) as List<T>

        companion object : AutoFind.Creator<List<out CharSequence>>() {
            override val checkByReflectWhenCall = ListCharSequenceType<CharSequence>(null)
            override val commonCase = ListCharSequenceType<CharSequence>(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<List<out CharSequence>> = ListCharSequenceType(isMarkedNullable)
        }
    }

    class ListIntegerType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<List<Int>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): List<Int> = emptyList()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: List<Int>
        ) = bundle.putIntegerArrayList(name, value.asArrayList())

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): List<Int>? = bundle.getIntegerArrayList(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: List<Int>
        ): Intent = intent.putExtra(name, value.asArrayList())

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): List<Int>? = intent.getIntegerArrayListExtra(name)

        companion object : AutoFind.Creator<List<Int>>() {
            override val checkByReflectWhenCall = ListIntegerType(null)
            override val commonCase = ListIntegerType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<List<Int>> = ListIntegerType(isMarkedNullable)
        }
    }
    //</editor-fold>

    //<editor-fold desc="类类型的数组" defaultstatus="collapsed">
    class ArrayParcelableType<T : Parcelable>(
        private val clazz: Class<T>?,
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<Array<T>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Array<T> =
            (clazz ?: property.returnType.argument0TypeJClass())
                .PARCELABLE_CREATOR.newArray(0)

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Array<T>
        ) = bundle.putParcelableArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Array<T>? {
            val clazz = clazz ?: property.returnType.argument0TypeJClass<T>()
            val array = BundleCompat.getParcelableArray(bundle, name, clazz)
                ?: return null
            val out = clazz.PARCELABLE_CREATOR.newArray(array.size)
            System.arraycopy(array, 0, out, 0, array.size)
            return out
        }

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Array<T>
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Array<T>? {
            val clazz = clazz ?: property.returnType.argument0TypeJClass<T>()
            val array = IntentCompat.getParcelableArrayExtra(intent, name, clazz)
                ?: return null
            val out = clazz.PARCELABLE_CREATOR.newArray(array.size)
            System.arraycopy(array, 0, out, 0, array.size)
            return out
        }

        companion object : AutoFind.Creator<Array<Parcelable>>() {
            override val checkByReflectWhenCall = ArrayParcelableType<Parcelable>(null, null)
            override val commonCase = ArrayParcelableType<Parcelable>(null, false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Array<Parcelable>> = ArrayParcelableType(
                type.argument0TypeClass(), isMarkedNullable
            )

            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified T : Parcelable> invoke() =
                ArrayParcelableType(T::class.java, false)

            @Suppress("UNCHECKED_CAST")
            inline fun <reified T : Parcelable> nullable(): BundleSupportType<Array<T>?> =
                ArrayParcelableType(T::class.java, true) as BundleSupportType<Array<T>?>
        }
    }

    class ArrayStringType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<Array<String>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Array<String> = arrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Array<String>
        ) = bundle.putStringArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Array<String>? = bundle.getStringArray(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Array<String>
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Array<String>? = intent.getStringArrayExtra(name)

        companion object : AutoFind.Creator<Array<String>>() {
            override val checkByReflectWhenCall = ArrayStringType(null)
            override val commonCase = ArrayStringType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Array<String>> = ArrayStringType(isMarkedNullable)
        }
    }

    class ArrayCharSequenceType(
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<Array<out CharSequence>>(isMarkedNullable) {
        override fun nonnullValue(property: KProperty<*>): Array<out CharSequence> = arrayOf()
        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Array<out CharSequence>
        ) = bundle.putCharSequenceArray(name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Array<out CharSequence>? = bundle.getCharSequenceArray(name)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Array<out CharSequence>
        ): Intent = intent.putExtra(name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Array<out CharSequence>? = intent.getCharSequenceArrayExtra(name)

        companion object : AutoFind.Creator<Array<out CharSequence>>() {
            override val checkByReflectWhenCall = ArrayCharSequenceType(null)
            override val commonCase = ArrayCharSequenceType(false)
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Array<out CharSequence>> = ArrayCharSequenceType(isMarkedNullable)
        }
    }
    //</editor-fold>

    //<editor-fold desc="扩展支持" defaultstatus="collapsed">
    /**
     * 枚举类型，存储时存放的是它的[Enum.name]
     */
    class EnumType<T : Enum<T & Any>?>(
        private val values: Array<T & Any>?,
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<T>(isMarkedNullable) {
        constructor(
            clazz: Class<T & Any>, isMarkedNullable: Boolean?
        ) : this(clazz.enumConstants, isMarkedNullable)

        override fun nonnullValue(property: KProperty<*>): T & Any =
            (values ?: property.getReturnTypeJClass<T & Any>().enumConstants).first()

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: T & Any
        ) = bundle.putString(name, value.name)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): T? = bundle.getString(name)?.let { forName(property, it) }

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: T & Any
        ): Intent = intent.putExtra(name, value.name)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): T? = intent.getStringExtra(name)?.let { forName(property, it) }

        private fun forName(
            property: KProperty<*>, name: String
        ): T? = values?.find { it.name == name }
            ?: java.lang.Enum.valueOf(property.getReturnTypeJClass(), name)

        @Suppress("UNCHECKED_CAST")
        companion object : AutoFind.Creator<Enum<*>>() {
            override val checkByReflectWhenCall = EnumType(null, null) as BundleSupportType<Enum<*>>
            override val commonCase = EnumType(null, false) as BundleSupportType<Enum<*>>

            @Suppress("kotlin:S6531")
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Enum<*>> = EnumType(
                type.tClass<Any>() as Class<out Enum<*>>, isMarkedNullable
            ) as BundleSupportType<Enum<*>>

            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified E : Enum<E>> invoke() = EnumType(enumValues<E>(), false)
            inline fun <reified E : Enum<E>> nullable(): BundleSupportType<E?> =
                EnumType(enumValues<E>(), true)
        }
    }

    /**
     * 使用[Parceler]来序列化的支持，[getParceler]
     */
    abstract class ParcelerType<T>(
        isMarkedNullable: Boolean?
    ) : BundleSupportType<T>(isMarkedNullable) {
        /**
         * 获取反序列化器，如有必要可通过 [property] 来获取，
         * 例如通过 [KProperty.getReturnTypeJClass] 来获取泛型参数的类型
         *
         * @param property
         * @return
         */
        protected abstract fun getParceler(property: KProperty<*>): Parceler<T?>

        private fun parseData(property: KProperty<*>, data: ByteArray?): T? =
            if (data == null) null else ParcelUtil.unmarshall(data, getParceler(property))

        override fun nonnullValue(property: KProperty<*>): T & Any =
            property.getReturnTypeJClass<T & Any>().getDeclaredConstructor().newInstance()

        //<editor-fold desc="覆写父类的读写方法" defaultstatus="collapsed">
        final override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: T & Any
        ) = bundle.putByteArray(name, ParcelUtil.marshall(value, getParceler(property)))

        final override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): T? = parseData(property, bundle.getByteArray(name))

        final override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: T?
        ): T = super.getNonnull(bundle, property, name, defaultValue)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: T & Any
        ): Intent = intent.putExtra(name, ParcelUtil.marshall(value, getParceler(property)))

        final override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): T? = parseData(property, intent.getByteArrayExtra(name))

        final override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: T?
        ): T = super.getExtraNonnull(intent, property, name, defaultValue)
        //</editor-fold>

        class ByParceler<T>(
            private val parceler: Parceler<T?>,
            isMarkedNullable: Boolean
        ) : ParcelerType<T>(isMarkedNullable) {
            override fun getParceler(property: KProperty<*>): Parceler<T?> = parceler
        }

        companion object {
            operator fun <T> invoke(parceler: Parceler<T?>) = ByParceler(parceler, false)

            @Suppress("UNCHECKED_CAST")
            fun <T> nullable(parceler: Parceler<T?>): BundleSupportType<T?> =
                ByParceler(parceler, true) as BundleSupportType<T?>
        }
    }

    //<editor-fold desc="Protobuf类型的序列化器" defaultstatus="collapsed>
    /**
     * Protobuf 类型，存储的是它的[MessageLite.toByteArray]
     *
     * @param writeClassName 如果 [parser] 为空，是否需要在序列化时同时序列化类名；
     * 如果[parser] 不为null，则无效
     */
    class ProtoBufType<T : MessageLite?>(
        private val parser: Parser<T & Any>?,
        private val writeClassName: Boolean,
        isMarkedNullable: Boolean? = false
    ) : BundleSupportType<T>(isMarkedNullable) {
        // Protobuf生成的类都是final的，如果是最终生成的实体类则不需要存储类名
        constructor(clazz: Class<T & Any>, isMarkedNullable: Boolean?) : this(
            if (Modifier.FINAL !in clazz.modifiers)
                null else clazz.protobufParserForType,
            Modifier.FINAL !in clazz.modifiers,
            isMarkedNullable
        )

        //<editor-fold desc="重写父类实现" defaultstatus="collapsed">
        override fun nonnullValue(property: KProperty<*>): T & Any =
            byteArrayOf().parseData(property)

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: T & Any
        ) {
            bundle.putByteArray(name, value.toByteArrayExt())
        }

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): T? = bundle.getByteArray(name)?.parseData(property)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: T & Any
        ): Intent = intent.putExtra(name, value.toByteArrayExt())

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): T? = intent.getByteArrayExtra(name)?.parseData(property)
        //</editor-fold>

        private fun <TA : T & Any> TA.toByteArrayExt(): ByteArray =
            if (parser == null && writeClassName) {
                // 如果没有传入反序列化器，并要求写入类名，以让反序列化时能找到对应的类型
                val className = javaClass.name.toByteArray()
                className.size.toByteArray() + className + toByteArray()
            } else {
                // 否则直接序列化数据即可，不需要写入类名信息
                toByteArray()
            }

        private fun ByteArray.parseData(
            property: KProperty<*>
        ): T & Any = if (!writeClassName) {
            // 如果没有要求写入类名，则直接使用传入的反序列化器
            val parser = parser ?: run {
                // 如果也没有传入反序列化器，通过反射获取字段类型的反序列化器
                val returnJClass = property.getReturnTypeJClass<T & Any>()
                require(Modifier.FINAL in returnJClass.modifiers) {
                    "对于没有传入 parser 且字段类型未精确到实体类的Protobuf字段委托，需要设置 writeClassName 为true"
                }
                returnJClass.protobufParserForType
            }
            parser.parseFrom(this)
        } else DataInputStream(ByteArrayInputStream(this)).use { input ->
            // 读取类名长度，然后读取类名字符串，最后使用该类的反序列化器解析数据
            val size = input.readInt()
            val className = String(input.readNBytesCompat(size))
            @Suppress("UNCHECKED_CAST")
            (Class.forName(className) as Class<T & Any>).protobufParserForType.parseFrom(input)
        }

        companion object : AutoFind.Creator<MessageLite>() {
            override val checkByReflectWhenCall = ProtoBufType<MessageLite>(null, true, null)
            override val commonCase = ProtoBufType<MessageLite>(
                null, writeClassName = false, isMarkedNullable = false
            )

            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<MessageLite> = ProtoBufType(
                type.tClass(), isMarkedNullable
            )

            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified T : MessageLite> invoke() =
                ProtoBufType(T::class.java, isMarkedNullable = false)

            @Suppress("UNCHECKED_CAST")
            inline fun <reified T : MessageLite> nullable(): BundleSupportType<T?> =
                ProtoBufType(T::class.java, isMarkedNullable = true) as BundleSupportType<T?>
        }
    }

    /**
     * List<Protobuf> 类型，存储的是它的[MessageLite.toByteArray]
     *
     * @param writeClassName 如果 [parser] 为空，是否需要在序列化时同时序列化类名；
     * 如果[parser] 不为null，则无效
     */
    class ListProtoBufType<T : MessageLite>(
        parser: Parser<T>?,
        writeClassName: Boolean,
        isMarkedNullable: Boolean? = false
    ) : ParcelerType<List<T?>>(isMarkedNullable) {
        // Protobuf生成的类都是final的，如果是最终生成的实体类则不需要存储类名
        constructor(
            clazz: Class<T>, isMarkedNullable: Boolean?
        ) : this(
            if (Modifier.FINAL !in clazz.modifiers)
                null else clazz.protobufParserForType,
            Modifier.FINAL !in clazz.modifiers,
            isMarkedNullable
        )

        private val parceler: Parceler<List<T?>?>? = if (parser != null) {
            // 如果传入 parser 反序列化器，则直接使用 ProtobufListParceler
            ProtobufListParceler(parser)
        } else if (writeClassName) {
            // 如果需要存储类名，则使用 ProtobufListParceler.Instance
            @Suppress("UNCHECKED_CAST", "kotlin:S6531")
            ProtobufListParceler.Instance as Parceler<List<T?>?>
        } else null

        override fun getParceler(property: KProperty<*>): Parceler<List<T?>?> {
            return parceler ?: run {
                // 如果没有根据 parceler 获取到反序列化器（即没有传入 parser 且不要写入类名）
                // 则反射获取 T 的类型并通过 T 的类型获取反序列化器，并包装为 ProtobufListParceler
                val arg0JClass = property.returnType.argument0TypeJClass<T>()
                require(Modifier.FINAL in arg0JClass.modifiers) {
                    "对于没有传入 parser 且字段类型未精确到实体类的Protobuf字段委托，需要设置 writeClassName 为true"
                }
                ProtobufListParceler(arg0JClass.protobufParserForType)
            }
        }

        override fun nonnullValue(property: KProperty<*>): List<T?> = emptyList()

        companion object : AutoFind.Creator<List<MessageLite?>>() {
            override val checkByReflectWhenCall = ListProtoBufType<MessageLite>(null, true, null)
            override val commonCase = ListProtoBufType<MessageLite>(
                null, writeClassName = false, isMarkedNullable = false
            )

            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<List<MessageLite?>> = ListProtoBufType(
                type.argument0TypeClass(), isMarkedNullable
            )

            /**
             * 通过 reified inline 获取 [T] 的类对象
             */
            inline operator fun <reified T : MessageLite> invoke() =
                ListProtoBufType(T::class.java, false)

            @Suppress("UNCHECKED_CAST")
            inline fun <reified T : MessageLite> nullable(): BundleSupportType<List<T>?> =
                ListProtoBufType(T::class.java, true) as BundleSupportType<List<T>?>
        }
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="对外暴露自动寻找类型接口" defaultstatus="collapsed">
    //<editor-fold desc="占位用于不支持的类型的报错信息反馈" defaultstatus="collapsed">
    class NotSupportType : BundleSupportType<Any>(false) {
        override fun nonnullValue(property: KProperty<*>): Any =
            throw IllegalArgumentException("Not support return type: $property")

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Any
        ) = throw IllegalArgumentException("Not support return type: $property")

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Any? = throw IllegalArgumentException("Not support return type: $property")

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Any?
        ): Any = throw IllegalArgumentException("Not support return type: $property")

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Any
        ): Intent = throw IllegalArgumentException("Not support return type: $property")

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Any? = throw IllegalArgumentException("Not support return type: $property")

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Any?
        ): Any = throw IllegalArgumentException("Not support return type: $property")

        companion object : AutoFind.Creator<Any>() {
            override val checkByReflectWhenCall = NotSupportType()
            override val commonCase = checkByReflectWhenCall
            override fun byType(
                type: AutoFind.TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<Any> = checkByReflectWhenCall
        }
    }
    //</editor-fold>

    /**
     * 通过反射字段类型来获取其访问器
     *
     * 每次调用都会使用[KProperty.returnType]，首次使用类和字段的耗时会比较大，一般不要直接使用这个单例，
     * 而是使用[AutoFind.findByType]来通过 inline 方法根据类型直接获取其存取器
     */
    object AutoFind : BundleSupportType<Any>(null) {
        //<editor-fold desc="重写父类实现">
        override fun nonnullValue(property: KProperty<*>): Any =
            findType(property).nonnullValue(property)

        override fun putNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, value: Any
        ) = findType(property).putNonnull(bundle, property, name, value)

        override fun getNullable(
            bundle: Bundle, property: KProperty<*>, name: String
        ): Any? = findType(property).getNullable(bundle, property, name)

        override fun getNonnull(
            bundle: Bundle, property: KProperty<*>, name: String, defaultValue: Any?
        ): Any = findType(property).getNonnull(bundle, property, name, defaultValue)

        override fun putExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, value: Any
        ): Intent = findType(property).putExtraNonnull(intent, property, name, value)

        override fun getExtraNullable(
            intent: Intent, property: KProperty<*>, name: String
        ): Any? = findType(property).getExtraNullable(intent, property, name)

        override fun getExtraNonnull(
            intent: Intent, property: KProperty<*>, name: String, defaultValue: Any?
        ): Any = findType(property).getExtraNonnull(intent, property, name, defaultValue)

        @Suppress("kotlin:S6530")
        private fun findType(property: KProperty<*>): BundleSupportType<Any> = findByType(
            TypeInfo.ByKProperty(property), NullableCheck.CHECK_BY_REFLECT_WHEN_CALL
        )
        //</editor-fold>

        inline fun <reified T> findByType(
            isMarkedNullable: Boolean?
        ): BundleSupportType<T> = findByType(
            TypeInfo.ByInline<T>(isMarkedNullable), NullableCheck.formBoolean(isMarkedNullable)
        )

        @Suppress("CyclomaticComplexMethod", "kotlin:S1479")
        fun <T> findByType(
            type: TypeInfo, checkNullable: NullableCheck
        ): BundleSupportType<T> {
            val clazz = type.tClass<Any>()
            val creator: Creator<*> = when {
                // 基础数据类型与其数组
                clazz == java.lang.Byte::class.java || clazz == java.lang.Byte.TYPE -> ByteType
                clazz == ByteArray::class.java -> ByteArrayType
                clazz == java.lang.Short::class.java || clazz == java.lang.Short.TYPE -> ShortType
                clazz == ShortArray::class.java -> ShortArrayType
                clazz == Integer::class.java || clazz == Integer.TYPE -> IntType
                clazz == IntArray::class.java -> IntArrayType
                clazz == java.lang.Long::class.java || clazz == java.lang.Long.TYPE -> LongType
                clazz == LongArray::class.java -> LongArrayType
                clazz == java.lang.Float::class.java || clazz == java.lang.Float.TYPE -> FloatType
                clazz == FloatArray::class.java -> FloatArrayType
                clazz == java.lang.Double::class.java || clazz == java.lang.Double.TYPE -> DoubleType
                clazz == DoubleArray::class.java -> DoubleArrayType
                clazz == java.lang.Boolean::class.java || clazz == java.lang.Boolean.TYPE -> BooleanType
                clazz == BooleanArray::class.java -> BooleanArrayType
                clazz == Character::class.java || clazz == Character.TYPE -> CharType
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
                    val arg0Class = type.argument0TypeClass<Any>()
                    when {
                        arg0Class.isSubclassOf(Parcelable::class.java) -> ListParcelableType
                        arg0Class == String::class.java -> ListStringType
                        arg0Class.isSubclassOf(CharSequence::class.java) -> ListCharSequenceType
                        arg0Class == Integer::class.java -> ListIntegerType
                        DependencyChecker.protobuf != null && arg0Class.isSubclassOf(MessageLite::class.java) ->
                            ListProtoBufType
                        else -> NotSupportType
                    }
                }
                // 数组类型
                clazz.isSubclassOf(Array<Parcelable>::class.java) -> ArrayParcelableType
                clazz == Array<String>::class.java -> ArrayStringType
                clazz.isSubclassOf(Array<CharSequence>::class.java) -> ArrayCharSequenceType
                // 扩展支持
                clazz.isSubclassOf(Enum::class.java) -> EnumType
                DependencyChecker.protobuf != null && clazz.isSubclassOf(MessageLite::class.java) ->
                    ProtoBufType
                // 原生的非final类型（protobuf 标准版的实体类都实现了 Serializable 接口，避免使用其为protobuf序列化）
                clazz.isSubclassOf(Serializable::class.java) -> SerializableType
                else -> NotSupportType
            }
            @Suppress("UNCHECKED_CAST")
            return when (checkNullable) {
                NullableCheck.NONNULL -> creator.byType(type, false)
                NullableCheck.NULLABLE -> creator.byType(type, true)
                NullableCheck.CHECK_NOW_BY_REFLECT -> creator.byType(type, type.isMarkedNullable)
                NullableCheck.CHECK_BY_REFLECT_WHEN_CALL -> creator.checkByReflectWhenCall
            } as BundleSupportType<T>
        }

        /**
         * 值的空安全检查处理类型，用于获取 [Creator.checkByReflectWhenCall] 或传入 [Creator.byType] 中使用
         *
         * - [NullableCheck.NONNULL] ：值非空，不为null
         * - [NullableCheck.NULLABLE] ：值可空
         * - [NullableCheck.CHECK_NOW_BY_REFLECT] ：立即检查值的nullable信息，通过调用 [TypeInfo.isMarkedNullable] 来检查 nullable 信息，
         * 该过程可能会调用到Kotlin反射
         * - [NullableCheck.CHECK_BY_REFLECT_WHEN_CALL] ：调用 [Creator.checkByReflectWhenCall] 来获取默认实现，并在调用读写时访问
         * [KProperty.returnType] 的 [KType.isMarkedNullable] 来获取nullable信息。
         * 不对外使用，仅在 [AutoFind] 中使用，即 [AutoFind.findByType] 方法中，用于提供通用实现中的读写委托。
         */
        enum class NullableCheck {
            NONNULL, NULLABLE, CHECK_NOW_BY_REFLECT,
            /* internal */ CHECK_BY_REFLECT_WHEN_CALL;

            companion object {
                fun formBoolean(
                    isMarkedNullable: Boolean?
                ) = when (isMarkedNullable) {
                    true -> NULLABLE
                    false -> NONNULL
                    null -> CHECK_NOW_BY_REFLECT
                }
            }
        }

        /**
         * 每个具体类型 [BundleSupportType] 的实现都需要提供一个此接口的单例实现，
         * 用于提供其默认实例（需要通过反射 [KProperty] 解析 nullable 并提供读写支持）、
         * 通过给定 [TypeInfo] 与 nullable 信息返回一个新实例用于处理该类型的读写支持。
         */
        abstract class Creator<T> {
            /**
             * 用于提供其默认实例（需要通过反射 [KProperty] 解析 nullable 并提供读写支持）
             */
            internal abstract val checkByReflectWhenCall: BundleSupportType<T>

            /**
             * 通用用例，非空的（[isMarkedNullable] 为false）
             *
             * 仅用于 IntentSetter [io.github.chenfei0928.content.set]
             */
            internal abstract val commonCase: BundleSupportType<T & Any>

            /**
             * 通过给定 [type] 与 [isMarkedNullable] 信息返回一个新实例用于处理该类型 [T] 的读写支持。
             */
            internal abstract fun byType(
                type: TypeInfo, isMarkedNullable: Boolean
            ): BundleSupportType<T>
        }

        /**
         * 储存、解析并提供类型信息、nullable信息
         */
        sealed class TypeInfo(
            private val jClass: Class<*>?,
        ) {
            protected abstract val kType: KType
            protected abstract val jType: Type?
            internal abstract val isMarkedNullable: Boolean

            class ByKProperty(
                private val property: KProperty<*>
            ) : TypeInfo(null) {
                override val kType: KType
                    get() = property.returnType
                override val jType: Type? = null
                override val isMarkedNullable: Boolean = kType.isMarkedNullable
            }

            abstract class ByInline(
                jClass: Class<*>,
                private val privateIsMarkedNullable: Boolean?,
            ) : TypeInfo(jClass) {
                //<editor-fold desc="kType与jType字段的getter懒加载与是否空标记" defaultstatus="collapsed">
                @field:Volatile
                final override var kType: KType
                    field : KType? = null
                    private set
                    get() = field ?: synchronized(this) {
                        field ?: run {
                            val type = kType()
                            field = type
                            type
                        }
                    }

                final override val isMarkedNullable: Boolean
                    get() = privateIsMarkedNullable ?: kType.isMarkedNullable

                protected abstract fun kType(): KType
                //</editor-fold>

                companion object {
                    inline operator fun <reified T> invoke(
                        isMarkedNullable: Boolean?
                    ) = object : ByInline(T::class.java, isMarkedNullable) {
                        override fun kType(): KType = typeOf<T>()
                        override val jType: Type by LazyTypeToken.Lazy<T>()
                    }
                }
            }

            @Suppress("UNCHECKED_CAST")
            internal fun <T> tClass(): Class<T> =
                (jClass ?: jType?.jvmErasureClassOrNull<Any>() ?: kType.jvmErasure.java) as Class<T>

            @Suppress("UNCHECKED_CAST")
            internal fun <T : Any> argument0TypeClass(): Class<T> = if (jClass?.isArray == true) {
                jClass.componentType as Class<T>
            } else if (jType == null) {
                kType.argument0TypeJClass()
            } else when (val tType = jType) {
                is ParameterizedType -> {
                    // List<Xxx>
                    tType.actualTypeArguments[0].jvmErasureClassOrNull<Any>() as Class<T>
                }
                is GenericArrayType -> {
                    // Array<Xxx>
                    tType.genericComponentType as Class<T>
                }
                is Class<*> -> {
                    tType.componentType as Class<T>
                }
                else -> {
                    throw IllegalArgumentException("Not support type: $tType")
                }
            }
        }
    }
    //</editor-fold>

    companion object {
        private const val TAG = "KW_BundleSupportType"

        @Suppress("UNCHECKED_CAST")
        private fun <T : Any> KProperty<*>.getReturnTypeJClass(): Class<T> =
            returnType.jvmErasure.java as Class<T>

        @Suppress("UNCHECKED_CAST")
        private fun <T : Any> KType.argument0TypeJClass(): Class<T> =
            arguments[0].type?.jvmErasure?.java as Class<T>
    }
}
