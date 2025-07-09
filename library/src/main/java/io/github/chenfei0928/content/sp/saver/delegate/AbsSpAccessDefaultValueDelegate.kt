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
sealed class AbsSpAccessDefaultValueDelegate<SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        V>
constructor(
    private val key: String?,
    final override val spValueType: PreferenceType<V & Any>,
    final override val defaultValue: V,
) : AbsSpSaver.AbsSpDelegate<SpSaver, Sp, Ed, V>, AbsSpSaver.DefaultValue<V> {
    final override fun getLocalStorageKey(property: KProperty<*>): String =
        key ?: property.name

    final override fun getValue(thisRef: SpSaver, property: KProperty<*>): V {
        // 允许子类处理key不存在时返回默认值
        return if (property in thisRef) {
            getValueImpl(thisRef.sp, getLocalStorageKey(property))
        } else {
            defaultValue
        }
    }

    final override fun setValue(thisRef: SpSaver, property: KProperty<*>, value: V) {
        val key = getLocalStorageKey(property)
        val editor = thisRef.editor
        // put null时直接remove掉key，交由子类处理时均是nonnull
        if (value == null) {
            editor.remove(key)
        } else {
            putValue(thisRef.editor, key, value)
        }
    }

    protected abstract fun getValueImpl(sp: Sp, key: String): V & Any
    protected abstract fun putValue(editor: Ed, key: String, value: V & Any)

    override fun toString(): String {
        return "${this.javaClass.simpleName}(key=$key, defaultValue=$defaultValue)"
    }
}
