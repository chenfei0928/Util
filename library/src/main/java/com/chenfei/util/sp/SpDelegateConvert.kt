package com.chenfei.util.sp

import android.content.SharedPreferences
import com.chenfei.util.kotlin.mapToIntArray
import kotlin.reflect.KProperty

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-09-03 13:38
 */
abstract class SpConvertSaver<T, R>(
    private val saver: AbsSpSaver.AbsSpDelegate<T>
) : AbsSpSaver.AbsSpDelegate0<R>() {

    override fun obtainDefaultKey(property: KProperty<*>): String {
        return saver.obtainDefaultKey(property)
    }

    override fun getValue(sp: SharedPreferences, key: String): R {
        return onRead(saver.getValue(sp, key))
    }

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: R) {
        saver.putValue(editor, key, onSave(value))
    }

    abstract fun onRead(value: T): R
    abstract fun onSave(value: R): T
}

class IntArraySpConvertSaver(
    saver: AbsSpSaver.AbsSpDelegate<String?>
) : SpConvertSaver<String?, IntArray>(saver) {

    constructor(name: String) : this(StringNullableDelegate(name))

    override fun onRead(value: String?): IntArray {
        if (value.isNullOrBlank()) {
            return IntArray(0)
        }
        return value
            .split(",")
            .mapToIntArray { it.toIntOrNull() ?: -1 }
    }

    override fun onSave(value: IntArray): String {
        return value.joinToString(",")
    }
}
