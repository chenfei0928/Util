package io.github.chenfei0928.preference.sp

import android.content.SharedPreferences.Editor
import android.util.Log
import androidx.preference.PreferenceManager
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.preference.BasePreferenceDataStore
import io.github.chenfei0928.preference.FieldAccessor
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
@Suppress("TooManyFunctions")
class SpSaverPreferenceDataStore<SpSaver : AbsSpSaver<SpSaver>>
constructor(
    internal val saver: SpSaver,
    fieldAccessor: SpSaverFieldAccessor<SpSaver> = SpSaverFieldAccessor.Impl(saver),
) : BasePreferenceDataStore<SpSaver>(fieldAccessor),
    SpSaverFieldAccessor<SpSaver> by fieldAccessor {

    //<editor-fold desc="根据 KProperty 获取Field、委托、字段名" defaultstatus="collapsed">
    private fun findFieldByProperty(
        property: KProperty<*>
    ): FieldAccessor.Field<SpSaver, *>? = properties.values.find {
        val property0 = if (it is SpSaverFieldAccessor.Impl.SpSaverField) {
            it.property0
        } else if (it is FieldAccessor.Impl.ReadCacheField
            && it.field is SpSaverFieldAccessor.Impl.SpSaverField
        ) {
            it.field.property0
        } else {
            return@find false
        }
        property0.name == property.name
    }

    /**
     * 根据 [property] 查找对应的委托
     *
     * 需要其委托创建最后阶段最后调用
     * [io.github.chenfei0928.content.sp.saver.DataStoreDelegateStoreProvider.dataStore] 来存储该委托信息，
     * 或调用任意 [property] 方法来获取并存储委托，如果没有存储将会返回 null ，该方法不会产生反射或其它长耗时调用
     */
    internal fun <V> findDelegateByProperty(property: KProperty<V>): AbsSpSaver.AbsSpDelegate<V>? {
        val field: SpSaverFieldAccessor.Impl.SpSaverField<SpSaver, V>? =
            findFieldByProperty(property) as? SpSaverFieldAccessor.Impl.SpSaverField<SpSaver, V>
        return field?.outDelegate
    }

    /**
     * 根据 [property] 查询对应的委托，如果查找不到则通过反射获取其委托
     */
    internal fun <V> getDelegateByProperty(property: KProperty<V>): AbsSpSaver.AbsSpDelegate<V> {
        return findDelegateByProperty(property) ?: run {
            Log.w(TAG, "getDelegateByProperty: $property in ${properties.keys.joinToString()}")
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
            require(delegate0 is AbsSpSaver.AbsSpDelegate<*>) {
                "Property($property) must is delegate subclass as AbsSpSaver.AbsSpDelegate0: $delegate0"
            }
            delegate0 as AbsSpSaver.AbsSpDelegate<V>
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
    internal fun findFieldNameByPropertyOrThrow(property: KProperty<*>): String {
        return findFieldByProperty(property)?.pdsKey
            ?: throw IllegalArgumentException("Not registered property: $property in ${properties.keys.joinToString()}")
    }

    /**
     * 查询指定 [property] 在持久化后的 spKey，用于向 [android.content.SharedPreferences] 注册监听的回调时使用
     */
    internal fun <V> getSpKeyByProperty(property: KProperty<V>): String =
        getDelegateByProperty(property).obtainDefaultKey(property)
    //</editor-fold>

    override fun <V> FieldAccessor.Field<SpSaver, V>.set(value: V) {
        setValue(saver, value)
        saver.apply()
    }

    override fun <V> FieldAccessor.Field<SpSaver, V>.get(): V =
        getValue(saver)

    companion object {
        private const val TAG = "SpSaverPreferenceDataSt"
    }
}
