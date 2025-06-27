/**
 * @author chenf()
 * @date 2025-06-27 16:17
 */
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
import io.github.chenfei0928.os.ParcelUtil
import kotlinx.parcelize.Parceler
import java.lang.reflect.Modifier
import kotlin.reflect.KProperty

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

operator fun Intent.set(property: KProperty<ByteArray>, value: ByteArray) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<ShortArray>, value: ShortArray) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<IntArray>, value: IntArray) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<LongArray>, value: LongArray) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<FloatArray>, value: FloatArray) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<DoubleArray>, value: DoubleArray) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<CharArray>, value: CharArray) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<BooleanArray>, value: BooleanArray) =
    putExtra(property.name, value)

operator fun Intent.set(property: KProperty<CharSequence>, value: CharSequence) =
    putExtra(property.name, value)

@JvmName("setCharSequenceArray")
operator fun Intent.set(
    property: KProperty<Array<out CharSequence>>,
    value: Array<out CharSequence>
) =
    putExtra(property.name, value)

@JvmName("setCharSequenceArray")
operator fun Intent.set(
    property: KProperty<List<out CharSequence>>,
    value: List<out CharSequence>
) =
    putExtra(property.name, value.asArrayList())

operator fun Intent.set(property: KProperty<String>, value: String) =
    putExtra(property.name, value)

@JvmName("setStringArray")
operator fun Intent.set(property: KProperty<Array<String>>, value: Array<String>) =
    putExtra(property.name, value)

@JvmName("setStringList")
operator fun Intent.set(property: KProperty<Array<String>>, value: List<String>) =
    putExtra(property.name, value.asArrayList())

operator fun Intent.set(property: KProperty<Bundle>, value: Bundle) =
    putExtra(property.name, value)

@JvmName("setParcelable")
operator fun <V : Parcelable, V1 : V> Intent.set(property: KProperty<V>, value: V1?) =
    putExtra(property.name, value)

@JvmName("setParcelableArray")
operator fun <V : Parcelable, V1 : V> Intent.set(property: KProperty<Array<V>>, value: Array<V1>?) =
    putExtra(property.name, value)

@JvmName("setParcelableList")
operator fun <V : Parcelable, V1 : V> Intent.set(property: KProperty<List<V>>, value: List<V1>?) =
    putExtra(property.name, value?.asArrayList())

@JvmName("setEnum")
operator fun <V : Enum<V>> Intent.set(property: KProperty<V>, value: V?) =
    putExtra(property.name, value)

/**
 * Set
 * [io.github.chenfei0928.os.BundleSupportType.ProtoBufType]
 *
 * @param V
 * @param property
 * @param value
 */
@ReturnThis
@JvmName("setProtobuf")
inline operator fun <reified V : MessageLite, V1 : V> Intent.set(
    property: KProperty<V>, value: V1?
): Intent {
    val writeClassName = Modifier.FINAL !in V::class.java.modifiers
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
 * Set
 * [io.github.chenfei0928.os.BundleSupportType.ListProtoBufType]
 *
 * @param V
 * @param property
 * @param value
 */
@JvmName("setProtobufList")
inline operator fun <reified V : MessageLite, V1 : V> Intent.set(
    property: KProperty<List<V>>, value: List<V1>?
): Intent {
    val writeClassName = Modifier.FINAL !in V::class.java.modifiers
    val parceler = if (writeClassName) {
        ProtobufListParceler.Instance as Parceler<List<V?>?>
    } else {
        ProtobufListParceler(V::class.java.protobufParserForType)
    }
    putExtra(property.name, ParcelUtil.marshall(value, parceler))
    return this
}

operator fun <V, V1 : V> Intent.set(
    property: KProperty<V>, parceler: Parceler<V>, value: V1
): Intent = putExtra(property.name, ParcelUtil.marshall(value, parceler))
