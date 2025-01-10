package io.github.chenfei0928.app.fragment

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.google.protobuf.MessageLite
import com.google.protobuf.protobufDefaultInstance
import io.github.chenfei0928.os.BundleSupportType
import io.github.chenfei0928.os.ReadOnlyCacheDelegate
import kotlinx.parcelize.Parceler
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * [Fragment] 使用 argument 存储数据时的读写委托
 *
 * @param T 字段数据类型
 * @property supportType 该类型的读写方式。如果可以，尽量不要使用[BundleSupportType.AutoFind]，
 * 它自动查找实现会使用到kt反射，首次反射某个类时对性能影响较大
 * @property name 字段在存储时对应的key的，传null时使用字段名
 * @property defaultValue 如果字段类型是非空，且数据源中没有对应数据时返回的默认值
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class ArgumentDelegate<T>(
    private val supportType: BundleSupportType<T>,
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
        inline operator fun <reified T> invoke(
            isMarkedNullable: Boolean = false, name: String? = null, defaultValue: T? = null
        ): ReadWriteProperty<Fragment, T> = ArgumentDelegate(
            BundleSupportType.AutoFind.findByType(isMarkedNullable), name, defaultValue
        )

        operator fun <T> invoke(
            parceler: Parceler<T?>,
            isMarkedNullable: Boolean = false,
            name: String? = null,
            defaultValue: T? = null
        ): ReadWriteProperty<Fragment, T> = ArgumentDelegate<T>(
            BundleSupportType.ParcelerType(parceler, isMarkedNullable), name, defaultValue
        )

        inline operator fun <reified T : MessageLite> invoke(
            isMarkedNullable: Boolean = false, name: String? = null,
        ): ReadWriteProperty<Fragment, T> = ArgumentDelegate(
            BundleSupportType.ProtoBufType<T>(isMarkedNullable),
            name,
            if (isMarkedNullable) null else T::class.java.protobufDefaultInstance
        )

        fun Fragment.argInt(name: String? = null): ReadWriteProperty<Fragment, Int> =
            ArgumentDelegate(BundleSupportType.IntType(false), name)

        fun Fragment.argBoolean(name: String? = null): ReadWriteProperty<Fragment, Boolean> =
            ArgumentDelegate(BundleSupportType.BooleanType(false), name)

        fun Fragment.argString(name: String? = null): ReadWriteProperty<Fragment, String> =
            ArgumentDelegate(BundleSupportType.StringType(false), name)

        fun Fragment.argStringNull(name: String? = null): ReadWriteProperty<Fragment, String?> =
            ArgumentDelegate(BundleSupportType.StringType(true) as BundleSupportType<String?>, name)

        inline fun <reified T : Parcelable> Fragment.argParcelable(
            name: String? = null
        ): ReadWriteProperty<Fragment, T> = ArgumentDelegate(
            BundleSupportType.ParcelableType(T::class.java, false), name
        )

        inline fun <reified T : Parcelable> Fragment.argParcelableNull(
            name: String? = null
        ): ReadWriteProperty<Fragment, T?> = ArgumentDelegate<T?>(
            BundleSupportType.ParcelableType(T::class.java, true) as BundleSupportType<T?>, name
        )

        inline fun <reified T : Parcelable> Fragment.argParcelableList(
            name: String? = null
        ): ReadWriteProperty<Fragment, List<T>> = ArgumentDelegate(
            BundleSupportType.ListParcelableType(T::class.java, false), name
        )
    }
}
