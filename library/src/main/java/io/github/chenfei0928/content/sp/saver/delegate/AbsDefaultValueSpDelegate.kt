package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType

/**
 * 根据构造器传入的key名或字段名来存取值，字段名将由kotlin负责维护，会在编译期生成而不会受到混淆的影响
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-06 15:51
 */
abstract class AbsDefaultValueSpDelegate<T>(
    key: String?, spValueType: PreferenceType, protected val defaultValue: T
) : AbsSpSaver.AbsSpDelegate0<T>(key, spValueType) {

    override fun getValue(sp: SharedPreferences, key: String): T {
        return if (sp.contains(key)) {
            getValueImpl(sp, key)
        } else {
            defaultValue
        }
    }

    protected abstract fun getValueImpl(sp: SharedPreferences, key: String): T

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: T) {
        if (value == null) {
            editor.remove(key)
        } else {
            putValueImpl(editor, key, value)
        }
    }

    protected abstract fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: T)
}
