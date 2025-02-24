package io.github.chenfei0928.app.fragment

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.google.protobuf.MessageLite
import com.google.protobuf.protobufDefaultInstance
import io.github.chenfei0928.os.BundleSupportType
import io.github.chenfei0928.os.ReadOnlyCacheDelegate
import kotlinx.parcelize.Parceler
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * [Fragment] 使用宿主 Activity 的 intent 存储数据时的读委托
 *
 * @param T 字段数据类型
 * @property supportType 该类型的读写方式。如果可以，尽量不要使用[BundleSupportType.AutoFind]，
 * 它自动查找实现会使用到kt反射，首次反射某个类时对性能影响较大
 * @property name 字段在存储时对应的key的，传null时使用字段名
 * @property defaultValue 如果字段类型是非空，且数据源中没有对应数据时返回的默认值
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2024-11-12 11:04
 */
class HostIntentDelegate<V>(
    private val supportType: BundleSupportType<V>,
    private val name: String? = null,
    private val defaultValue: V? = null,
) : ReadOnlyCacheDelegate<Fragment, V>() {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): V {
        return thisRef.requireActivity().intent.run {
            setExtrasClassLoader(thisRef.requireActivity().classLoader)
            supportType.getValue(this, property, name ?: property.name, defaultValue)
        }
    }

    companion object {
        inline operator fun <reified V> invoke(
            isMarkedNullable: Boolean = false, name: String? = null, defaultValue: V? = null
        ): ReadOnlyProperty<Fragment, V> = HostIntentDelegate(
            BundleSupportType.AutoFind.findByType<V>(isMarkedNullable), name, defaultValue
        )

        operator fun <V> invoke(
            parceler: Parceler<V?>,
            name: String? = null,
            defaultValue: V? = null
        ): ReadOnlyProperty<Fragment, V> = HostIntentDelegate(
            BundleSupportType.ParcelerType<V>(parceler), name, defaultValue
        )

        inline operator fun <reified V : MessageLite> invoke(
            name: String? = null,
        ): ReadOnlyProperty<Fragment, V> = HostIntentDelegate(
            BundleSupportType.ProtoBufType<V>(), name, V::class.java.protobufDefaultInstance
        )

        fun Fragment.intentInt(name: String? = null): ReadOnlyProperty<Fragment, Int> =
            HostIntentDelegate(BundleSupportType.IntType(false), name)

        fun Fragment.intentBoolean(name: String? = null): ReadOnlyProperty<Fragment, Boolean> =
            HostIntentDelegate(BundleSupportType.BooleanType(false), name)

        fun Fragment.intentString(name: String? = null): ReadOnlyProperty<Fragment, String> =
            HostIntentDelegate(BundleSupportType.StringType(false), name)

        fun Fragment.intentStringNull(name: String? = null): ReadOnlyProperty<Fragment, String?> =
            HostIntentDelegate(BundleSupportType.StringType(true), name)

        inline fun <reified V : Parcelable> Fragment.intentParcelable(
            name: String? = null
        ): ReadOnlyProperty<Fragment, V> = HostIntentDelegate(
            BundleSupportType.ParcelableType(V::class.java, false), name
        )

        inline fun <reified V : Parcelable> Fragment.intentParcelableNull(
            name: String? = null
        ): ReadOnlyProperty<Fragment, V?> = HostIntentDelegate(
            BundleSupportType.ParcelableType(V::class.java, true), name
        )

        inline fun <reified V : Parcelable> Fragment.intentParcelableList(
            name: String? = null
        ): ReadOnlyProperty<Fragment, List<V>> = HostIntentDelegate(
            BundleSupportType.ListParcelableType(V::class.java, false), name
        )
    }
}
