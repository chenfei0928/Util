package io.github.chenfei0928.app.fragment

import android.os.Parcelable
import androidx.fragment.app.Fragment
import io.github.chenfei0928.os.BundleSupportType
import io.github.chenfei0928.os.ReadOnlyCacheDelegate
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * [Fragment] 使用 argument 存储数据时的读写委托
 *
 * @param T 字段数据类型
 * @property supportType 该类型的读写方式
 * @property name 字段在存储时对应的key的，传null时使用字段名
 * @property defaultValue 如果字段类型是非空，且数据源中没有对应数据时返回的默认值
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class ArgumentDelegate<T>(
    private val supportType: BundleSupportType<T> = BundleSupportType.AutoFind as BundleSupportType<T>,
    private val name: String? = null,
    private val defaultValue: T? = null,
) : ReadOnlyCacheDelegate<Fragment, T>(), ReadWriteProperty<Fragment, T> {
    override fun getValueImpl(thisRef: Fragment, property: KProperty<*>): T {
        return thisRef.requireArguments().run {
            classLoader = thisRef.javaClass.classLoader
            supportType.getValue(this, property, name ?: property.name, defaultValue)
        }
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        this.value = value
        thisRef.applyArgumentBundle {
            supportType.putNullable(this, property, name ?: property.name, value)
        }
    }

    companion object {
        fun Fragment.argInt(): ReadWriteProperty<Fragment, Int> =
            ArgumentDelegate(BundleSupportType.IntType(false))

        fun Fragment.argBoolean(): ReadWriteProperty<Fragment, Boolean> =
            ArgumentDelegate(BundleSupportType.BooleanType(false))

        fun Fragment.argString(): ReadWriteProperty<Fragment, String> =
            ArgumentDelegate(BundleSupportType.StringType(false))

        inline fun <reified T : Parcelable> Fragment.argParcelable(): ReadWriteProperty<Fragment, T> =
            ArgumentDelegate(BundleSupportType.ParcelableType(T::class.java, false))

        inline fun <reified T : Parcelable> Fragment.argParcelableList(): ReadWriteProperty<Fragment, List<T>> =
            ArgumentDelegate(BundleSupportType.ListParcelableType(T::class.java, false))
    }
}
