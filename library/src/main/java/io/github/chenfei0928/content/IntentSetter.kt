/**
 * @author chenf()
 * @date 2025-06-27 16:17
 */
@file:Suppress("TooManyFunctions")

package io.github.chenfei0928.content

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.ReturnThis
import com.google.protobuf.MessageLite
import com.google.protobuf.ProtobufListParceler
import com.google.protobuf.protobufParserForType
import io.github.chenfei0928.app.activity.ActivityDelegate
import io.github.chenfei0928.app.fragment.HostIntentDelegate
import io.github.chenfei0928.base.UtilInitializer
import io.github.chenfei0928.lang.contains
import io.github.chenfei0928.os.BundleSupportType
import io.github.chenfei0928.os.ParcelUtil
import kotlinx.parcelize.Parceler
import java.lang.reflect.Modifier
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

interface IntentSetter<V> {
    fun contains(intent: Intent, property: KProperty<*>): Boolean
    fun putValue(intent: Intent, property: KProperty<*>, value: V?)
}

@ReturnThis
operator fun <V, V1 : V> Intent.set(
    property: KProperty<V>, delegate: IntentSetter<V>, value: V1?
): Intent {
    delegate.putValue(this, property, value)
    return this
}

/**
 * 对于不设置 [ActivityDelegate.name] 、 [HostIntentDelegate.name] 与 [IntentDelegate.name] 的委托属性上，
 * 可以直接调用该方法获取其是否已经设置到 [Intent] 中。
 */
operator fun Intent.contains(
    property: KProperty<*>
): Boolean = if (
    UtilInitializer.sdkDependency.ktKPropertyCompiledDelegate && property is KProperty0
) {
    property.isAccessible = true
    (property.getDelegate() as IntentSetter<*>).contains(this, property)
} else {
    hasExtra(property.name)
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
) = setImpl(property, BundleSupportType.ByteArrayType.commonCase, value)

operator fun Intent.set(
    property: KProperty<ShortArray>, value: ShortArray?
) = setImpl(property, BundleSupportType.ShortArrayType.commonCase, value)

operator fun Intent.set(
    property: KProperty<IntArray>, value: IntArray?
) = setImpl(property, BundleSupportType.IntArrayType.commonCase, value)

operator fun Intent.set(
    property: KProperty<LongArray>, value: LongArray?
) = setImpl(property, BundleSupportType.LongArrayType.commonCase, value)

operator fun Intent.set(
    property: KProperty<FloatArray>, value: FloatArray?
) = setImpl(property, BundleSupportType.FloatArrayType.commonCase, value)

operator fun Intent.set(
    property: KProperty<DoubleArray>, value: DoubleArray?
) = setImpl(property, BundleSupportType.DoubleArrayType.commonCase, value)

operator fun Intent.set(
    property: KProperty<CharArray>, value: CharArray?
) = setImpl(property, BundleSupportType.CharArrayType.commonCase, value)

operator fun Intent.set(
    property: KProperty<BooleanArray>, value: BooleanArray?
) = setImpl(property, BundleSupportType.BooleanArrayType.commonCase, value)

operator fun Intent.set(
    property: KProperty<CharSequence>, value: CharSequence?
) = setImpl(property, BundleSupportType.CharSequenceType.commonCase, value)

@JvmName("setCharSequenceArray")
operator fun Intent.set(
    property: KProperty<Array<out CharSequence>>, value: Array<out CharSequence>?
) = setImpl(property, BundleSupportType.ArrayCharSequenceType.commonCase, value)

@JvmName("setCharSequenceArray")
operator fun Intent.set(
    property: KProperty<List<out CharSequence>>, value: List<out CharSequence>?
) = setImpl(property, BundleSupportType.ListCharSequenceType.commonCase, value)

operator fun Intent.set(
    property: KProperty<String>, value: String?
) = setImpl(property, BundleSupportType.StringType.commonCase, value)

@JvmName("setStringArray")
operator fun Intent.set(
    property: KProperty<Array<String>>, value: Array<String>?
) = setImpl(property, BundleSupportType.ArrayStringType.commonCase, value)

@JvmName("setStringList")
operator fun Intent.set(
    property: KProperty<Array<String>>, value: List<String>?
) = setImpl(
    property,
    BundleSupportType.ListStringType.commonCase as BundleSupportType<Any?>,
    value
)

operator fun Intent.set(
    property: KProperty<Bundle>, value: Bundle?
) = setImpl(property, BundleSupportType.BundleType.commonCase, value)

@JvmName("setParcelable")
operator fun <V : Parcelable, V1 : V> Intent.set(
    property: KProperty<V>, value: V1?
) = setImpl(property, BundleSupportType.ParcelableType.commonCase, value)

@JvmName("setParcelableArray")
operator fun <V : Parcelable, V1 : V> Intent.set(
    property: KProperty<Array<V>>, value: Array<V1>?
) = setImpl(
    property,
    BundleSupportType.ArrayParcelableType.commonCase as BundleSupportType<Any>,
    value
)

@JvmName("setParcelableList")
operator fun <V : Parcelable, V1 : V> Intent.set(
    property: KProperty<List<V>>, value: List<V1>?
) = setImpl(property, BundleSupportType.ListParcelableType.commonCase, value)

@JvmName("setEnum")
operator fun <V : Enum<V>> Intent.set(
    property: KProperty<V>, value: V?
) = setImpl(property, BundleSupportType.EnumType.commonCase, value)
//</editor-fold>

@ReturnThis
private fun <V, V1 : V> Intent.setImpl(
    property: KProperty<V>, bundleSupportType: BundleSupportType<V>, value: V1?
): Intent {
    if (UtilInitializer.sdkDependency.ktKPropertyCompiledDelegate && property is KProperty0) {
        property.isAccessible = true
        (property.getDelegate() as IntentSetter<V>).putValue(this, property, value)
    } else {
        bundleSupportType.putExtraNullable(this, property, property.name, value)
    }
    return this
}

/**
 * 实现与 [ActivityDelegate.Companion.protobuf]、[BundleSupportType.ProtoBufType.invoke] 一致
 *
 * 即在 [V] 为抽象类型时，行为与 [BundleSupportType.ProtoBufType.checkByReflectWhenCall] 一致，
 * [V] 为具体类型时，行为与 [BundleSupportType.ProtoBufType.commonCase] 一致
 *
 * [BundleSupportType.ProtoBufType.toByteArrayExt]
 *
 * 在 [V] 为具体类型时，行为与 [putExtra] 一致，并兼容
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
    val type = if (writeClassName) {
        BundleSupportType.ProtoBufType.checkByReflectWhenCall
    } else {
        BundleSupportType.ProtoBufType.commonCase
    }
    this.setImpl(property, type as BundleSupportType<V?>, value)
    return this
}

/**
 * 实现与 [ActivityDelegate.Companion.protobuf] 或 [BundleSupportType.ListProtoBufType.invoke] 一致
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
@ReturnThis
operator fun <V, V1 : V> Intent.set(
    property: KProperty<V>, parceler: Parceler<V>, value: V1?
): Intent {
    if (value == null) {
        removeExtra(property.name)
    } else {
        putExtra(property.name, ParcelUtil.marshall(value, parceler))
    }
    return this
}
