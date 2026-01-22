package io.github.chenfei0928.preference.sp

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.preference.PreferenceManager
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.DataStoreDelegateStoreProvider.Companion.dataStore
import io.github.chenfei0928.lang.toStringAny
import io.github.chenfei0928.preference.base.BaseFieldAccessorCache
import io.github.chenfei0928.preference.base.FieldAccessor
import io.github.chenfei0928.preference.sp.SpSaverFieldAccessor.Companion.property
import io.github.chenfei0928.preference.sp.SpSaverFieldAccessor.Field
import io.github.chenfei0928.util.Log
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

/**
 * 用于对spSaver的扩展，以优化[PreferenceManager]的访问
 *
 * 实现了对写入数据时自动保存[Editor.apply]
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-04-24 10:42
 */
class SpSaverFieldAccessorCache<SpSaver : AbsSpSaver<SpSaver, *, *>>
constructor(
    internal val saver: SpSaver,
    fieldAccessor: SpSaverFieldAccessor<SpSaver> = SpSaverFieldAccessor.Impl(saver),
) : BaseFieldAccessorCache<SpSaver>(fieldAccessor),
    SpSaverFieldAccessor<SpSaver> by fieldAccessor {

    //<editor-fold desc="根据 KProperty 获取Field、委托、字段名" defaultstatus="collapsed">
    internal inline val spSaverPropertyDelegateFields: List<Field<SpSaver, out Any?>>
        get() = properties.values.mapNotNull {
            FieldAccessor.FieldWrapper.findByType<Field<SpSaver, Any>, SpSaver, Any>(it)
        }

    /**
     * 根据 [property] 查找对应的委托
     *
     * 该方法支持的 [V] 的类型仅限于 [SharedPreferences] 所原生支持的数据类型和 `Enum`、`Set<Enum>`
     *
     * 需要其委托创建最后阶段最后调用
     * [io.github.chenfei0928.content.sp.saver.DataStoreDelegateStoreProvider.dataStore] 来存储该委托信息，
     * 或调用任意 [property] 方法来获取并存储委托
     *
     * 如果没有存储将会返回 null ，该方法不会产生反射或其它长耗时调用
     */
    @Suppress("UNCHECKED_CAST")
    internal fun <V> findFieldOrNullByProperty(
        property: KProperty<V>
    ): Field<SpSaver, V>? = spSaverPropertyDelegateFields.find {
        it.property.name == property.name
    } as? Field<SpSaver, V>

    /**
     * 根据 [property] 查询对应的委托，如果查找不到则通过反射获取其委托
     */
    @Suppress("UNCHECKED_CAST")
    internal fun <V> getDelegateOrByReflect(property: KProperty<V>): AbsSpSaver.Delegate<SpSaver, V> {
        return findFieldOrNullByProperty(property)?.outDelegate ?: run {
            Log.d(TAG, buildString {
                append("getDelegateByProperty: getDelegate fallback to kotlin reflect call, maybe slow, because ")
                append(property)
                append(" not found in ")
                properties.keys.joinTo(this)
            })
            val delegate0 = when (property) {
                is KProperty0<*> -> {
                    property.isAccessible = true
                    property.getDelegate()
                }
                is KProperty1<*, *> -> {
                    property.isAccessible = true
                    @Suppress("UNCHECKED_CAST")
                    (property as KProperty1<SpSaver, *>).getDelegate(saver)
                }
                else -> throw IllegalArgumentException("not support KProperty2 or other Property: $property")
            }
            // 判断该字段的委托
            require(delegate0 is AbsSpSaver.Delegate<*, *>) {
                "Property($property) must is delegate subclass as AbsSpSaver.Delegate: $delegate0"
            }
            delegate0 as AbsSpSaver.Delegate<SpSaver, V>
        }
    }

    /**
     * 根据 [property] 查找对应的字段名，用于将自身注册给 [PreferenceManager.setPreferenceDataStore]
     * 后查询指定 [property] 在当前注册的[FieldAccessor.Field.pdsKey]
     *
     * 需要其委托创建最后阶段最后调用
     * [io.github.chenfei0928.content.sp.saver.DataStoreDelegateStoreProvider.dataStore] 来存储该委托信息，
     * 或调用任意 [property] 方法来获取并存储委托，如果没有存储将会返回 null ，该方法不会产生反射或其它长耗时调用
     */
    internal fun <V> findFieldByPropertyOrThrow(property: KProperty<V>): Field<SpSaver, V> {
        return findFieldOrNullByProperty(property)
            ?: throw IllegalArgumentException("Not registered property: $property in ${properties.keys.joinToString()}")
    }

    /**
     * 查询指定 [property] 在持久化后的 spKey，用于向 [android.content.SharedPreferences] 注册监听的回调时使用
     */
    internal fun <V> getSpKeyByProperty(property: KProperty<V>): String =
        getDelegateOrByReflect(property).getLocalStorageKey(property)

    /**
     * 对所有 field 进行 toString，其读取字段方式为访问 [Field.get] 方法，而非访问 [Field.property] 以优化性能。
     *
     * 但如果调用 [SpSaverFieldAccessor.property] 传入 `findSpAccessorDelegateIfStructAndHasDelegate` 值为 `true` 时
     * 可能会造成读取结构体时输出反序列化前的原始信息而非结构体的 `toString` 方法。
     */
    internal fun toSpSaverPropertyString(): String =
        saver.toStringAny(fields = spSaverPropertyDelegateFields.toTypedArray())
    //</editor-fold>

    /**
     * 通知一个字段被变更；并返回受影响的field的key，即 [androidx.preference.Preference.getKey] 的集合
     */
    internal fun onPropertyChange(localStorageKey: String): Collection<String> {
        return properties.mapNotNull {
            val spSaverField = FieldAccessor.FieldWrapper
                .findByType<FieldAccessor.SpLocalStorageKey, SpSaver, Any>(it.value)
            if (spSaverField?.localStorageKey == localStorageKey) {
                // 此处无需更新 FieldAccessor.Impl.ReadCacheField ，sp的fieldAccessor不开启readCache
                it.key
            } else {
                null
            }
        }
    }

    override fun <V> FieldAccessor.Field<SpSaver, V>.setToStorage(value: V) {
        setValue(saver, value)
        saver.apply()
    }

    override fun <V> FieldAccessor.Field<SpSaver, V>.getFromStorage(): V =
        getValue(saver)

    companion object {
        private const val TAG = "Ut_SpSaverFieldAccCache"
    }
}
