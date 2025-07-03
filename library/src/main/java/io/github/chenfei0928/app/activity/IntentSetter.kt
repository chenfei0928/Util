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
import io.github.chenfei0928.lang.contains
import io.github.chenfei0928.os.BundleSupportType
import io.github.chenfei0928.os.ParcelUtil
import kotlinx.parcelize.Parceler
import java.lang.reflect.Modifier
import kotlin.reflect.KProperty

operator fun <V, V1 : V> Intent.set(
    property: KProperty<V>, delegate: IntentDelegate<V>, value: V1?
): Intent {
    delegate.putValue(this, property, value)
    return this
}

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
) = BundleSupportType.ByteArrayType.commonCase
    .putExtraNullable(this, property, property.name, value)

operator fun Intent.set(
    property: KProperty<ShortArray>, value: ShortArray?
) = BundleSupportType.ShortArrayType.commonCase
    .putExtraNullable(this, property, property.name, value)

operator fun Intent.set(
    property: KProperty<IntArray>, value: IntArray?
) = BundleSupportType.IntArrayType.commonCase
    .putExtraNullable(this, property, property.name, value)

operator fun Intent.set(
    property: KProperty<LongArray>, value: LongArray?
) = BundleSupportType.LongArrayType.commonCase
    .putExtraNullable(this, property, property.name, value)

operator fun Intent.set(
    property: KProperty<FloatArray>, value: FloatArray?
) = BundleSupportType.FloatArrayType.commonCase
    .putExtraNullable(this, property, property.name, value)

operator fun Intent.set(
    property: KProperty<DoubleArray>, value: DoubleArray?
) = BundleSupportType.DoubleArrayType.commonCase
    .putExtraNullable(this, property, property.name, value)

operator fun Intent.set(
    property: KProperty<CharArray>, value: CharArray?
) = BundleSupportType.CharArrayType.commonCase
    .putExtraNullable(this, property, property.name, value)

operator fun Intent.set(
    property: KProperty<BooleanArray>, value: BooleanArray?
) = BundleSupportType.BooleanArrayType.commonCase
    .putExtraNullable(this, property, property.name, value)

operator fun Intent.set(
    property: KProperty<CharSequence>, value: CharSequence?
) = BundleSupportType.CharSequenceType.commonCase
    .putExtraNullable(this, property, property.name, value)

@JvmName("setCharSequenceArray")
operator fun Intent.set(
    property: KProperty<Array<out CharSequence>>, value: Array<out CharSequence>?
) = BundleSupportType.ArrayCharSequenceType.commonCase
    .putExtraNullable(this, property, property.name, value)

@JvmName("setCharSequenceArray")
operator fun Intent.set(
    property: KProperty<List<out CharSequence>>, value: List<out CharSequence>?
) = BundleSupportType.ListCharSequenceType.commonCase
    .putExtraNullable(this, property, property.name, value)

operator fun Intent.set(
    property: KProperty<String>, value: String?
) = BundleSupportType.StringType.commonCase
    .putExtraNullable(this, property, property.name, value)

@JvmName("setStringArray")
operator fun Intent.set(
    property: KProperty<Array<String>>, value: Array<String>?
) = BundleSupportType.ArrayStringType.commonCase
    .putExtraNullable(this, property, property.name, value)

@JvmName("setStringList")
operator fun Intent.set(
    property: KProperty<Array<String>>, value: List<String>?
) = BundleSupportType.ListStringType.commonCase
    .putExtraNullable(this, property, property.name, value)

operator fun Intent.set(
    property: KProperty<Bundle>, value: Bundle?
) = BundleSupportType.BundleType.commonCase
    .putExtraNullable(this, property, property.name, value)

@JvmName("setParcelable")
operator fun <V : Parcelable, V1 : V> Intent.set(
    property: KProperty<V>, value: V1?
) = BundleSupportType.ParcelableType.commonCase
    .putExtraNullable(this, property, property.name, value)

@JvmName("setParcelableArray")
operator fun <V : Parcelable, V1 : V> Intent.set(
    property: KProperty<Array<V>>, value: Array<V1>?
) = (BundleSupportType.ArrayParcelableType.commonCase as BundleSupportType<Array<V1>>)
    .putExtraNullable(this, property, property.name, value)

@JvmName("setParcelableList")
operator fun <V : Parcelable, V1 : V> Intent.set(
    property: KProperty<List<V>>, value: List<V1>?
) = BundleSupportType.ListParcelableType.commonCase
    .putExtraNullable(this, property, property.name, value)

@JvmName("setEnum")
operator fun <V : Enum<V>> Intent.set(
    property: KProperty<V>, value: V?
) = BundleSupportType.EnumType.commonCase
    .putExtraNullable(this, property, property.name, value)
//</editor-fold>

/**
 * 实现与 [IntentDelegate.invoke]、[BundleSupportType.ProtoBufType.invoke] 一致
 *
 * 即在 [V] 为抽象类型时，行为与 [BundleSupportType.ProtoBufType.checkByReflectWhenCall] 一致，
 * [V] 为具体类型时，行为与 [BundleSupportType.ProtoBufType.commonCase] 一致
 *
 * [BundleSupportType.ProtoBufType.toByteArrayExt]
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

@JvmName("setProtobuf")
operator fun <V : MessageLite, V1 : V> Intent.set(
    property: KProperty<V>, vClass: Class<V>, value: V1?
): Intent {
    val writeClassName = Modifier.FINAL !in vClass.modifiers
    val type = if (writeClassName)
        BundleSupportType.ProtoBufType.checkByReflectWhenCall
    else BundleSupportType.ProtoBufType.commonCase
    return type.putExtraNullable(this, property, property.name, value)
}

/**
 * 实现与 [IntentDelegate.invoke] 或 [BundleSupportType.ListProtoBufType.invoke] 一致
 *
 * [BundleSupportType.ListProtoBufType.parceler]
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
 * 实现与 [BundleSupportType.ParcelerType.putExtraNonnull] 的操作一致
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
