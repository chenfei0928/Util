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
class HostIntentDelegate<T>(
    private val supportType: BundleSupportType<T>,
    private val name: String? = null,
    private val defaultValue: T? = null,
) : ReadOnlyCacheDelegate<Fragment, T>() {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): T {
        return thisRef.requireActivity().intent.run {
            setExtrasClassLoader(thisRef.requireActivity().classLoader)
            supportType.getValue(this, property, name ?: property.name, defaultValue)
        }
    }

    companion object {
        inline operator fun <reified T> invoke(
            isMarkedNullable: Boolean = false, name: String? = null, defaultValue: T? = null
        ): ReadOnlyProperty<Fragment, T> = HostIntentDelegate(
            BundleSupportType.AutoFind.findByType(isMarkedNullable), name, defaultValue
        )

        operator fun <T> invoke(
            parceler: Parceler<T?>,
            isMarkedNullable: Boolean = false,
            name: String? = null,
            defaultValue: T? = null
        ): ReadOnlyProperty<Fragment, T> = HostIntentDelegate(
            BundleSupportType.ParcelerType(parceler, isMarkedNullable), name, defaultValue
        )

        inline operator fun <reified T : MessageLite> invoke(
            isMarkedNullable: Boolean = false, name: String? = null,
        ): ReadOnlyProperty<Fragment, T> = HostIntentDelegate(
            BundleSupportType.ProtoBufType<T>(isMarkedNullable),
            name,
            if (isMarkedNullable) null else T::class.java.protobufDefaultInstance
        )

        fun Fragment.intentInt(name: String? = null): ReadOnlyProperty<Fragment, Int> =
            HostIntentDelegate(BundleSupportType.IntType(false), name)

        fun Fragment.intentBoolean(name: String? = null): ReadOnlyProperty<Fragment, Boolean> =
            HostIntentDelegate(BundleSupportType.BooleanType(false), name)

        fun Fragment.intentString(name: String? = null): ReadOnlyProperty<Fragment, String> =
            HostIntentDelegate(BundleSupportType.StringType(false), name)

        fun Fragment.intentStringNull(name: String? = null): ReadOnlyProperty<Fragment, String?> =
            HostIntentDelegate(BundleSupportType.StringType(true), name)

        inline fun <reified T : Parcelable> Fragment.intentParcelable(
            name: String? = null
        ): ReadOnlyProperty<Fragment, T> = HostIntentDelegate(
            BundleSupportType.ParcelableType(T::class.java, false), name
        )

        inline fun <reified T : Parcelable> Fragment.intentParcelableNull(
            name: String? = null
        ): ReadOnlyProperty<Fragment, T?> = HostIntentDelegate(
            BundleSupportType.ParcelableType(T::class.java, true), name
        )

        inline fun <reified T : Parcelable> Fragment.intentParcelableList(
            name: String? = null
        ): ReadOnlyProperty<Fragment, List<T>> = HostIntentDelegate(
            BundleSupportType.ListParcelableType(T::class.java, false), name
        )
    }
}
