package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.AbsSpSaver.Companion.getSp
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
sealed class AbsSpAccessDefaultValueDelegate<SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        T>
constructor(
    private val key: String?,
    final override val spValueType: PreferenceType,
    final override val defaultValue: T,
) : AbsSpSaver.AbsSpDelegate<SpSaver, Sp, Ed, T>, AbsSpSaver.DefaultValue<T> {
    final override fun obtainDefaultKey(property: KProperty<*>): String =
        key ?: property.name

    final override fun getValue(thisRef: SpSaver, property: KProperty<*>): T {
        val sp = getSp(thisRef)
        val key = obtainDefaultKey(property)
        // 允许子类处理key不存在时返回默认值
        return if (sp.contains(key)) {
            getValueImpl(sp, key)
        } else {
            defaultValue
        }
    }

    final override fun setValue(thisRef: SpSaver, property: KProperty<*>, value: T) {
        val key = obtainDefaultKey(property)
        val editor = thisRef.editor
        // put null时直接remove掉key，交由子类处理时均是nonnull
        if (value == null) {
            editor.remove(key)
        } else {
            putValue(editor, key, value)
        }
    }

    protected abstract fun getValueImpl(sp: Sp, key: String): T & Any
    protected abstract fun putValue(editor: Ed, key: String, value: T & Any)
}
