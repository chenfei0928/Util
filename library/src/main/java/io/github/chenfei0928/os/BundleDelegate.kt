package io.github.chenfei0928.os

import android.os.Bundle
import android.os.Parcelable
import com.google.protobuf.MessageLite
import com.google.protobuf.protobufDefaultInstance
import kotlinx.parcelize.Parceler
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * [Bundle] 使用 argument 存储数据时的读写委托
 *
 * @param T 字段数据类型
 * @property supportType 该类型的读写方式。如果可以，尽量不要使用[BundleSupportType.AutoFind]，
 * 它自动查找实现会使用到kt反射，首次反射某个类时对性能影响较大
 * @property name 字段在存储时对应的key的，传null时使用字段名
 * @property defaultValue 如果字段类型是非空，且数据源中没有对应数据时返回的默认值
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class BundleDelegate<V>(
    private val supportType: BundleSupportType<V>,
    private val name: String? = null,
    private val defaultValue: V? = null,
) : ReadWriteProperty<Bundle, V> {
    override fun getValue(thisRef: Bundle, property: KProperty<*>): V {
        return supportType.getValue(thisRef, property, name ?: property.name, defaultValue) as V
    }

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: V) {
        supportType.putNullable(thisRef, property, name ?: property.name, value)
    }

    companion object {
        inline operator fun <reified V> invoke(
            isMarkedNullable: Boolean = false, name: String? = null, defaultValue: V? = null
        ): ReadWriteProperty<Bundle, V> = BundleDelegate(
            BundleSupportType.AutoFind.findByType<V>(isMarkedNullable), name, defaultValue
        )

        fun <V> parceler(
            parceler: Parceler<V?>,
            name: String? = null,
            defaultValue: V? = null
        ): ReadWriteProperty<Bundle, V> = BundleDelegate(
            BundleSupportType.ParcelerType(parceler), name, defaultValue
        )

        inline fun <reified V : MessageLite> protobuf(
            name: String? = null,
        ): ReadWriteProperty<Bundle, V> = BundleDelegate(
            BundleSupportType.ProtoBufType<V>(), name, V::class.java.protobufDefaultInstance
        )

        fun int(name: String? = null): ReadWriteProperty<Bundle, Int> =
            BundleDelegate(BundleSupportType.IntType(false), name)

        fun boolean(name: String? = null): ReadWriteProperty<Bundle, Boolean> =
            BundleDelegate(BundleSupportType.BooleanType(false), name)

        fun string(name: String? = null): ReadWriteProperty<Bundle, String> =
            BundleDelegate(BundleSupportType.StringType(false), name)

        @Suppress("UNCHECKED_CAST")
        fun stringNullable(name: String? = null): ReadWriteProperty<Bundle, String?> =
            BundleDelegate(BundleSupportType.StringType(true) as BundleSupportType<String?>, name)

        inline fun <reified V : Parcelable> parcelable(
            name: String? = null
        ): ReadWriteProperty<Bundle, V> = BundleDelegate(
            BundleSupportType.ParcelableType(V::class.java, false), name
        )

        @Suppress("UNCHECKED_CAST")
        inline fun <reified V : Parcelable> parcelableNullable(
            name: String? = null
        ): ReadWriteProperty<Bundle, V?> = BundleDelegate(
            BundleSupportType.ParcelableType(V::class.java, true) as BundleSupportType<V?>, name
        )

        inline fun <reified V : Parcelable> parcelableList(
            name: String? = null
        ): ReadWriteProperty<Bundle, List<V>> = BundleDelegate(
            BundleSupportType.ListParcelableType(V::class.java, false), name
        )
    }
}
