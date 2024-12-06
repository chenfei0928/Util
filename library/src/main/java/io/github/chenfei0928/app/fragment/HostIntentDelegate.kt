package io.github.chenfei0928.app.fragment

import android.os.Parcelable
import androidx.fragment.app.Fragment
import io.github.chenfei0928.os.ReadOnlyCacheDelegate
import io.github.chenfei0928.os.BundleSupportType
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * [Fragment] 使用宿主 Activity 的 intent 存储数据时的读委托
 *
 * @param T 字段数据类型
 * @property supportType 该类型的读写方式
 * @property name 字段在存储时对应的key的，传null时使用字段名
 * @property defaultValue 如果字段类型是非空，且数据源中没有对应数据时返回的默认值
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2024-11-12 11:04
 */
class HostIntentDelegate<T>(
    private val supportType: BundleSupportType<T> = BundleSupportType.AutoFind as BundleSupportType<T>,
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
        fun Fragment.intentInt(): ReadOnlyProperty<Fragment, Int> =
            HostIntentDelegate(BundleSupportType.IntType(false))

        fun Fragment.intentBoolean(): ReadOnlyProperty<Fragment, Boolean> =
            HostIntentDelegate(BundleSupportType.BooleanType(false))

        fun Fragment.intentString(): ReadOnlyProperty<Fragment, String> =
            HostIntentDelegate(BundleSupportType.StringType(false))

        inline fun <reified T : Parcelable> Fragment.intentParcelable(): ReadOnlyProperty<Fragment, T> =
            HostIntentDelegate(BundleSupportType.ParcelableType(T::class.java))

        inline fun <reified T : Parcelable> Fragment.intentParcelableList(): ReadOnlyProperty<Fragment, List<T>> =
            HostIntentDelegate(BundleSupportType.ListParcelableType(T::class.java))
    }
}
