package io.github.chenfei0928.content.sp.delegate

import android.content.SharedPreferences
import com.google.gson.Gson
import io.github.chenfei0928.collection.mapToIntArray
import java.lang.reflect.Type
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

class GsonSpConvertSaver<T>(
    saver: AbsSpSaver.AbsSpDelegate<String?>,
    private val gson: Gson = io.github.chenfei0928.util.gson.gson,
    private val type: Type
) : SpConvertSaver<String?, T?>(saver) {

    override fun onRead(value: String?): T? {
        return gson.fromJson<T>(value, type)
    }

    override fun onSave(value: T?): String? {
        return gson.toJson(value)
    }
}
