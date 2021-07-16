package com.chenfei.util.sp

import android.content.SharedPreferences

/**
 * 根据构造器传入的key名或字段名来存取值，字段名将由kotlin负责维护，会在编译期生成而不会受到混淆的影响
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-06 15:51
 */
abstract class AbsDefaultValueSpDelegate<T>(
    key: String?, protected val defaultValue: T
) : AbsSpSaver.AbsSpDelegate<T>(key) {

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

//<editor-fold defaultstate="collapsed" desc="Sp默认的各个支持直接存取的字段类型">
class StringDefaultValueDelegate(
    key: String? = null, defaultValue: String
) : AbsDefaultValueSpDelegate<String>(key, defaultValue) {
    override fun getValueImpl(sp: SharedPreferences, key: String): String =
        sp.getString(key, defaultValue) ?: defaultValue

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: String) {
        editor.putString(key, value)
    }
}

class StringSetDefaultValueDelegate(
    key: String? = null, defaultValue: Set<String>
) : AbsDefaultValueSpDelegate<Set<String>>(key, defaultValue) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Set<String> =
        sp.getStringSet(key, defaultValue) ?: defaultValue

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Set<String>) {
        editor.putStringSet(key, value)
    }
}

class IntDefaultValueDelegate(
    key: String? = null, defaultValue: Int
) : AbsDefaultValueSpDelegate<Int>(key, defaultValue) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Int =
        sp.getInt(key, defaultValue)

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Int) {
        editor.putInt(key, value)
    }
}

class LongDefaultValueDelegate(
    key: String? = null, defaultValue: Long
) : AbsDefaultValueSpDelegate<Long>(key, defaultValue) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Long =
        sp.getLong(key, defaultValue)

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Long) {
        editor.putLong(key, value)
    }
}

class BooleanDefaultValueDelegate(
    key: String? = null, defaultValue: Boolean
) : AbsDefaultValueSpDelegate<Boolean>(key, defaultValue) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Boolean =
        sp.getBoolean(key, defaultValue)

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Boolean) {
        editor.putBoolean(key, value)
    }
}

class FloatDefaultValueDelegate(
    key: String? = null, defaultValue: Float
) : AbsDefaultValueSpDelegate<Float>(key, defaultValue) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Float =
        sp.getFloat(key, defaultValue)

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Float) {
        editor.putFloat(key, value)
    }
}
//</editor-fold>
