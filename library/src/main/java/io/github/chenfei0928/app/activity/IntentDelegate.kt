package io.github.chenfei0928.app.activity

import android.app.Activity
import android.os.Parcelable
import io.github.chenfei0928.os.BundleSupportType
import io.github.chenfei0928.os.ReadOnlyCacheDelegate
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * [Activity] 使用 intent 存储数据时的读委托
 *
 * @param T 字段数据类型
 * @property supportType 该类型的读写方式
 * @property name 字段在存储时对应的key的，传null时使用字段名
 * @property defaultValue 如果字段类型是非空，且数据源中没有对应数据时返回的默认值
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class IntentDelegate<T>(
    private val supportType: BundleSupportType<T> = BundleSupportType.AutoFind as BundleSupportType<T>,
    private val name: String? = null,
    private val defaultValue: T? = null,
) : ReadOnlyCacheDelegate<Activity, T>() {
    override fun getValueImpl(thisRef: Activity, property: KProperty<*>): T {
        return thisRef.intent.run {
            setExtrasClassLoader(thisRef.classLoader)
            supportType.getValue(this, property, name ?: property.name, defaultValue)
        }
    }

    companion object {
        fun Activity.intentInt(): ReadOnlyProperty<Activity, Int> =
            IntentDelegate(BundleSupportType.IntType(false))

        fun Activity.intentBoolean(): ReadOnlyProperty<Activity, Boolean> =
            IntentDelegate(BundleSupportType.BooleanType(false))

        fun Activity.intentString(): ReadOnlyProperty<Activity, String> =
            IntentDelegate(BundleSupportType.StringType(false))

        inline fun <reified T : Parcelable> Activity.intentParcelable(): ReadOnlyProperty<Activity, T> =
            IntentDelegate(BundleSupportType.ParcelableType(T::class.java))

        inline fun <reified T : Parcelable> Activity.intentParcelableList(): ReadOnlyProperty<Activity, List<T>> =
            IntentDelegate(BundleSupportType.ListParcelableType(T::class.java))
    }
}
