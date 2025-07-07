package io.github.chenfei0928.content

import android.content.Intent
import android.os.Parcelable
import com.google.protobuf.MessageLite
import com.google.protobuf.protobufDefaultInstance
import io.github.chenfei0928.os.BundleSupportType
import kotlinx.parcelize.Parceler
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * [android.content.Intent] 使用 intent 存储数据时的读委托
 *
 * @param T 字段数据类型
 * @property supportType 该类型的读写方式。如果可以，尽量不要使用[io.github.chenfei0928.os.BundleSupportType.AutoFind]，
 * 它自动查找实现会使用到kt反射，首次反射某个类时对性能影响较大
 * @property name 字段在存储时对应的key的，传null时使用字段名
 * @property defaultValue 如果字段类型是非空，且数据源中没有对应数据时返回的默认值
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class IntentDelegate<V>(
    private val supportType: BundleSupportType<V>,
    private val name: String? = null,
    private val defaultValue: V? = null,
) : ReadOnlyProperty<Intent, V>, ReadWriteProperty<Intent, V>, IntentSetter<V> {
    override fun getValue(thisRef: Intent, property: KProperty<*>): V {
        return supportType.getValue(thisRef, property, name ?: property.name, defaultValue)
    }

    override fun setValue(thisRef: Intent, property: KProperty<*>, value: V) {
        putValue(thisRef, property, value)
    }

    override fun putValue(intent: Intent, property: KProperty<*>, value: V?) {
        supportType.putExtraNullable(intent, property, name ?: property.name, value)
    }

    companion object {
        inline operator fun <reified V> invoke(
            isMarkedNullable: Boolean = false, name: String? = null, defaultValue: V? = null
        ): ReadOnlyProperty<Intent, V> = IntentDelegate(
            BundleSupportType.AutoFind.findByType<V>(isMarkedNullable), name, defaultValue
        )

        operator fun <V> invoke(
            parceler: Parceler<V?>,
            name: String? = null,
            defaultValue: V? = null
        ): ReadOnlyProperty<Intent, V> = IntentDelegate(
            BundleSupportType.ParcelerType(parceler), name, defaultValue
        )

        inline operator fun <reified V : MessageLite> invoke(
            name: String? = null,
        ): ReadOnlyProperty<Intent, V> = IntentDelegate(
            BundleSupportType.ProtoBufType<V>(), name, V::class.java.protobufDefaultInstance
        )

        fun intentInt(name: String? = null): ReadOnlyProperty<Intent, Int> =
            IntentDelegate(BundleSupportType.IntType(false), name)

        fun intentBoolean(name: String? = null): ReadOnlyProperty<Intent, Boolean> =
            IntentDelegate(BundleSupportType.BooleanType(false), name)

        fun intentString(name: String? = null): ReadOnlyProperty<Intent, String> =
            IntentDelegate(BundleSupportType.StringType(false), name)

        fun intentStringNull(name: String? = null): ReadOnlyProperty<Intent, String?> =
            IntentDelegate(BundleSupportType.StringType(true), name)

        inline fun <reified V : Parcelable> intentParcelable(
            name: String? = null
        ): ReadOnlyProperty<Intent, V> = IntentDelegate(
            BundleSupportType.ParcelableType(V::class.java, false), name
        )

        inline fun <reified T : Parcelable> intentParcelableNull(
            name: String? = null
        ): ReadOnlyProperty<Intent, T?> = IntentDelegate(
            BundleSupportType.ParcelableType(T::class.java, true), name
        )

        inline fun <reified V : Parcelable> intentParcelableList(
            name: String? = null
        ): ReadOnlyProperty<Intent, List<V>> = IntentDelegate(
            BundleSupportType.ListParcelableType(V::class.java, false), name
        )
    }
}