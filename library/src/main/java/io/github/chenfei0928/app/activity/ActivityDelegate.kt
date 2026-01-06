package io.github.chenfei0928.app.activity

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import com.google.protobuf.MessageLite
import com.google.protobuf.protobufDefaultInstance
import io.github.chenfei0928.content.IntentSetter
import io.github.chenfei0928.os.BundleSupportType
import kotlinx.parcelize.Parceler
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * [Activity] 使用 intent 存储数据时的读委托
 *
 * @param T 字段数据类型
 * @property supportType 该类型的读写方式。如果可以，尽量不要使用[BundleSupportType.AutoFind]，
 * 它自动查找实现会使用到kt反射，首次反射某个类时对性能影响较大
 * @property name 字段在存储时对应的key的，传null时使用字段名
 * @property defaultValue 如果字段类型是非空，且数据源中没有对应数据时返回的默认值
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
class ActivityDelegate<V>(
    private val supportType: BundleSupportType<V>,
    private val name: String? = null,
    private val defaultValue: V? = null,
) : ReadOnlyProperty<Activity, V>, IntentSetter<V> {
    override fun getValue(thisRef: Activity, property: KProperty<*>): V {
        return thisRef.intent.run {
            setExtrasClassLoader(thisRef.classLoader)
            supportType.getValue(this, property, name ?: property.name, defaultValue) as V
        }
    }

    override fun putValue(intent: Intent, property: KProperty<*>, value: V?) {
        supportType.putExtraNullable(intent, property, name ?: property.name, value)
    }

    override fun contains(intent: Intent, property: KProperty<*>): Boolean {
        return intent.hasExtra(name ?: property.name)
    }

    companion object {
        inline operator fun <reified V> invoke(
            isMarkedNullable: Boolean?, name: String? = null, defaultValue: V? = null
        ): ReadOnlyProperty<Activity, V> = ActivityDelegate(
            BundleSupportType.AutoFind.findByType<V>(isMarkedNullable), name, defaultValue
        )

        fun <V> parceler(
            parceler: Parceler<V?>,
            name: String? = null,
            defaultValue: V? = null
        ): ReadOnlyProperty<Activity, V> = ActivityDelegate(
            BundleSupportType.ParcelerType(parceler), name, defaultValue
        )

        inline fun <reified V : MessageLite> protobuf(
            name: String? = null,
        ): ReadOnlyProperty<Activity, V> = ActivityDelegate(
            BundleSupportType.ProtoBufType<V>(), name, V::class.java.protobufDefaultInstance
        )

        fun int(name: String? = null): ReadOnlyProperty<Activity, Int> =
            ActivityDelegate(BundleSupportType.IntType(false), name)

        fun boolean(name: String? = null): ReadOnlyProperty<Activity, Boolean> =
            ActivityDelegate(BundleSupportType.BooleanType(false), name)

        fun string(name: String? = null): ReadOnlyProperty<Activity, String> =
            ActivityDelegate(BundleSupportType.StringType(false), name)

        fun stringNullable(name: String? = null): ReadOnlyProperty<Activity, String?> =
            ActivityDelegate(BundleSupportType.StringType(true), name)

        inline fun <reified V : Parcelable> parcelable(
            name: String? = null
        ): ReadOnlyProperty<Activity, V> = ActivityDelegate(
            BundleSupportType.ParcelableType(V::class.java, false), name
        )

        inline fun <reified T : Parcelable> parcelableNullable(
            name: String? = null
        ): ReadOnlyProperty<Activity, T?> = ActivityDelegate(
            BundleSupportType.ParcelableType(T::class.java, true), name
        )

        inline fun <reified V : Parcelable> parcelableList(
            name: String? = null
        ): ReadOnlyProperty<Activity, List<V>> = ActivityDelegate(
            BundleSupportType.ListParcelableType(V::class.java, false), name
        )
    }
}
