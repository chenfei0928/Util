package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import kotlin.reflect.KProperty

/**
 * 根据构造器传入的key名或字段名来存取值，字段名将由kotlin负责维护，会在编译期生成而不会受到混淆的影响
 *
 * 本类用来对实现类提供[AbsSpSaver.sp]、[AbsSpSaver.editor]字段的直接访问
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-06 15:51
 */
sealed class AbsSpAccessDefaultValueDelegate<SpSaver : AbsSpSaver<SpSaver>, T>(
    internal val key: String?,
    spValueType: PreferenceType.Native,
    protected val defaultValue: T,
) : AbsSpSaver.AbsSpDelegateImpl<SpSaver, T>(spValueType) {

    final override fun obtainDefaultKey(property: KProperty<*>): String =
        key ?: property.name

    final override fun getValue(sp: SharedPreferences, key: String): T {
        return if (sp.contains(key)) {
            getValueImpl(sp, key)
        } else {
            defaultValue
        }
    }

    protected abstract fun getValueImpl(sp: SharedPreferences, key: String): T
}
