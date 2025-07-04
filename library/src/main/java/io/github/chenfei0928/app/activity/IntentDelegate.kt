package io.github.chenfei0928.app.activity

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import com.google.protobuf.MessageLite
import com.google.protobuf.protobufDefaultInstance
import io.github.chenfei0928.os.BundleSupportType
import io.github.chenfei0928.os.ReadOnlyCacheDelegate
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
class IntentDelegate<V>(
    private val supportType: BundleSupportType<V>,
    private val name: String? = null,
    private val defaultValue: V? = null,
) : ReadOnlyCacheDelegate<Activity, V>() {
    override fun getValueImpl(thisRef: Activity, property: KProperty<*>): V {
        return thisRef.intent.run {
            setExtrasClassLoader(thisRef.classLoader)
            supportType.getValue(this, property, name ?: property.name, defaultValue)
        }
    }

    fun putValue(intent: Intent, property: KProperty<*>, value: V?) {
        supportType.putExtraNullable(intent, property, name ?: property.name, value)
    }

    companion object {
        inline operator fun <reified V> invoke(
            isMarkedNullable: Boolean = false, name: String? = null, defaultValue: V? = null
        ): ReadOnlyProperty<Activity, V> = IntentDelegate(
            BundleSupportType.AutoFind.findByType<V>(isMarkedNullable), name, defaultValue
        )

        operator fun <V> invoke(
            parceler: Parceler<V?>,
            name: String? = null,
            defaultValue: V? = null
        ): ReadOnlyProperty<Activity, V> = IntentDelegate(
            BundleSupportType.ParcelerType(parceler), name, defaultValue
        )

        inline operator fun <reified V : MessageLite> invoke(
            name: String? = null,
        ): ReadOnlyProperty<Activity, V> = IntentDelegate(
            BundleSupportType.ProtoBufType<V>(), name, V::class.java.protobufDefaultInstance
        )

        fun Activity.intentInt(name: String? = null): ReadOnlyProperty<Activity, Int> =
            IntentDelegate(BundleSupportType.IntType(false), name)

        fun Activity.intentBoolean(name: String? = null): ReadOnlyProperty<Activity, Boolean> =
            IntentDelegate(BundleSupportType.BooleanType(false), name)

        fun Activity.intentString(name: String? = null): ReadOnlyProperty<Activity, String> =
            IntentDelegate(BundleSupportType.StringType(false), name)

        fun Activity.intentStringNull(name: String? = null): ReadOnlyProperty<Activity, String?> =
            IntentDelegate(BundleSupportType.StringType(true), name)

        inline fun <reified V : Parcelable> Activity.intentParcelable(
            name: String? = null
        ): ReadOnlyProperty<Activity, V> = IntentDelegate(
            BundleSupportType.ParcelableType(V::class.java, false), name
        )

        inline fun <reified T : Parcelable> Activity.intentParcelableNull(
            name: String? = null
        ): ReadOnlyProperty<Activity, T?> = IntentDelegate(
            BundleSupportType.ParcelableType(T::class.java, true), name
        )

        inline fun <reified V : Parcelable> Activity.intentParcelableList(
            name: String? = null
        ): ReadOnlyProperty<Activity, List<V>> = IntentDelegate(
            BundleSupportType.ListParcelableType(V::class.java, false), name
        )
    }
}
