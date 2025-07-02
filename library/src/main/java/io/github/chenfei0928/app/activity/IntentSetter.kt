/**
 * @author chenf()
 * @date 2025-06-27 16:17
 */
@file:Suppress("TooManyFunctions")

package io.github.chenfei0928.app.activity

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.ReturnThis
import com.google.protobuf.MessageLite
import com.google.protobuf.ProtobufListParceler
import com.google.protobuf.protobufParserForType
import io.github.chenfei0928.collection.asArrayList
import io.github.chenfei0928.lang.contains
import io.github.chenfei0928.lang.toByteArray
import io.github.chenfei0928.os.BundleSupportType
import io.github.chenfei0928.os.ParcelUtil
import kotlinx.parcelize.Parceler
import java.lang.reflect.Modifier
import kotlin.reflect.KProperty

//<editor-fold desc="Intent所原生支持的类型" defaultstatus="collapsed">
operator fun Intent.set(property: KProperty<Byte>, value: Byte) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<Short>, value: Short) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<Int>, value: Int) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<Long>, value: Long) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<Float>, value: Float) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<Double>, value: Double) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<Char>, value: Char) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<Boolean>, value: Boolean) =
    putExtra(property.name, value)

operator fun Intent.set(
    property: KProperty<ByteArray>, value: ByteArray?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

operator fun Intent.set(
    property: KProperty<ShortArray>, value: ShortArray?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

operator fun Intent.set(
    property: KProperty<IntArray>, value: IntArray?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

operator fun Intent.set(
    property: KProperty<LongArray>, value: LongArray?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

operator fun Intent.set(
    property: KProperty<FloatArray>, value: FloatArray?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

operator fun Intent.set(
    property: KProperty<DoubleArray>, value: DoubleArray?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

operator fun Intent.set(
    property: KProperty<CharArray>, value: CharArray?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

operator fun Intent.set(
    property: KProperty<BooleanArray>, value: BooleanArray?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

operator fun Intent.set(
    property: KProperty<CharSequence>, value: CharSequence?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

@JvmName("setCharSequenceArray")
operator fun Intent.set(
    property: KProperty<Array<out CharSequence>>, value: Array<out CharSequence>?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

@JvmName("setCharSequenceArray")
operator fun Intent.set(
    property: KProperty<List<out CharSequence>>, value: List<out CharSequence>?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value.asArrayList())

operator fun Intent.set(
    property: KProperty<String>, value: String?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

@JvmName("setStringArray")
operator fun Intent.set(
    property: KProperty<Array<String>>, value: Array<String>?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

@JvmName("setStringList")
operator fun Intent.set(
    property: KProperty<Array<String>>, value: List<String>?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value.asArrayList())

operator fun Intent.set(
    property: KProperty<Bundle>, value: Bundle?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

@JvmName("setParcelable")
operator fun <V : Parcelable, V1 : V> Intent.set(
    property: KProperty<V>, value: V1?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

@JvmName("setParcelableArray")
operator fun <V : Parcelable, V1 : V> Intent.set(
    property: KProperty<Array<V>>, value: Array<V1>?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)

@JvmName("setParcelableList")
operator fun <V : Parcelable, V1 : V> Intent.set(
    property: KProperty<List<V>>, value: List<V1>?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value.asArrayList())

@JvmName("setEnum")
operator fun <V : Enum<V>> Intent.set(
    property: KProperty<V>, value: V?
) = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, value)
//</editor-fold>

/**
 * 实现与 [BundleSupportType.ProtoBufType.putNonnull] 一致
 *
 * 在 [V] 为具体类型时，行为与 [io.github.chenfei0928.content.putExtra] 一致，并兼容
 * [io.github.chenfei0928.content.getProtobufExtra]
 *
 * @param V
 * @param property
 * @param value
 */
@JvmName("setProtobuf")
inline operator fun <reified V : MessageLite, V1 : V> Intent.set(
    property: KProperty<V>, value: V1?
): Intent = set(property, V::class.java, value)

@ReturnThis
@JvmName("setProtobuf")
operator fun <V : MessageLite, V1 : V> Intent.set(
    property: KProperty<V>, vClass: Class<V>, value: V1?
): Intent {
    val writeClassName = Modifier.FINAL !in vClass.modifiers
    val byteArray = if (value == null) {
        removeExtra(property.name)
        return this
    } else if (writeClassName) {
        val className = value.javaClass.name.toByteArray()
        className.size.toByteArray() + className + value.toByteArray()
    } else {
        value.toByteArray()
    }
    putExtra(property.name, byteArray)
    return this
}

/**
 * 实现与 [BundleSupportType.ListProtoBufType.parceler] 的逆操作一致
 *
 * @param V
 * @param property
 * @param value
 */
@JvmName("setProtobufList")
inline operator fun <reified V : MessageLite, V1 : V> Intent.set(
    property: KProperty<List<V>>, value: List<V1>?
): Intent = set(property, V::class.java, value)

@ReturnThis
@JvmName("setProtobufList")
operator fun <V : MessageLite, V1 : V> Intent.set(
    property: KProperty<List<V>>, vClass: Class<V>, value: List<V1>?
): Intent {
    val writeClassName = Modifier.FINAL !in vClass.modifiers
    val parceler = if (value == null) {
        removeExtra(property.name)
        return this
    } else if (writeClassName) {
        ProtobufListParceler.Instance as Parceler<List<V?>?>
    } else {
        ProtobufListParceler(vClass.protobufParserForType)
    }
    putExtra(property.name, ParcelUtil.marshall(value, parceler))
    return this
}

/**
 * 实现与 [BundleSupportType.ParcelerType.parseData] 的逆操作一致
 *
 * @param V
 * @param V1
 * @param property
 * @param parceler
 * @param value
 * @return
 */
operator fun <V, V1 : V> Intent.set(
    property: KProperty<V>, parceler: Parceler<V>, value: V1?
): Intent = if (value == null) {
    removeExtra(property.name)
    this
} else putExtra(property.name, ParcelUtil.marshall(value, parceler))
