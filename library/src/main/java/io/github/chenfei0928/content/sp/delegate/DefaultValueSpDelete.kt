package io.github.chenfei0928.content.sp.delegate

import android.content.SharedPreferences
import kotlin.reflect.KProperty

/**
 * 使nullable的字段委托拥有默认值的装饰器
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-12 16:39
 */
class DefaultValueSpDelete<T>(
    private val saver: AbsSpSaver.AbsSpDelegate0<T>,
    private val defaultValue: T
) : AbsSpSaver.AbsSpDelegate0<T>() {

    override fun obtainDefaultKey(property: KProperty<*>): String {
        return saver.obtainDefaultKey(property)
    }

    override fun getValue(sp: SharedPreferences, key: String): T {
        return saver.getValue(sp, key) ?: defaultValue
    }

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: T) {
        saver.putValue(editor, key, value)
    }
}

fun <T> AbsSpSaver.AbsSpDelegate0<T>.defaultValue(defaultValue: T) =
    DefaultValueSpDelete(this, defaultValue)
