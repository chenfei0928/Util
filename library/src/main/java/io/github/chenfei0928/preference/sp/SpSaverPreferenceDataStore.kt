package io.github.chenfei0928.preference.sp

import android.util.Log
import androidx.preference.PreferenceManager
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.getPropertySpKeyName
import io.github.chenfei0928.preference.BasePreferenceDataStore
import io.github.chenfei0928.preference.FieldAccessor
import kotlin.reflect.KProperty

/**
 * 用于对spSaver的扩展，以优化[PreferenceManager]的访问
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-04-24 10:42
 */
@Suppress("TooManyFunctions")
class SpSaverPreferenceDataStore<SpSaver : AbsSpSaver>(
    internal val saver: SpSaver,
    fieldAccessor: SpSaverFieldAccessor<SpSaver>,
) : BasePreferenceDataStore<SpSaver>(fieldAccessor),
    SpSaverFieldAccessor<SpSaver> by fieldAccessor {

    constructor(
        saver: SpSaver,
        accessDelegateName: Boolean = false
    ) : this(saver, SpSaverFieldAccessor.Impl(saver, accessDelegateName))

    internal fun findFieldNameByProperty(property: KProperty<*>): String {
        val field = properties.values.find {
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
        if (field != null) {
            return field.name
        }
        Log.w(TAG, "findFieldNameByProperty: $property in ${properties.keys.joinToString()}")
        return saver.getPropertySpKeyName(property, true)
    }

    override fun <V> FieldAccessor.Field<SpSaver, V>.set(value: V) {
        setValue(saver, value)
    }

    override fun <V> FieldAccessor.Field<SpSaver, V>.get(): V =
        getValue(saver)

    companion object {
        private const val TAG = "SpSaverPreferenceDataSt"
    }
}
