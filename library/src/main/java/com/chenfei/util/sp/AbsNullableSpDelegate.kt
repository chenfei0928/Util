package com.chenfei.util.sp

import android.content.SharedPreferences

/**
 * 根据构造器传入的key名或字段名来存取值，字段名将由kotlin负责维护，会在编译期生成而不会受到混淆的影响
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-06 15:51
 */
abstract class AbsNullableSpDelegate<T>(
    key: String?
) : AbsSpSaver.AbsSpDelegate<T?>(key) {

    override fun getValue(sp: SharedPreferences, key: String): T? {
        return if (sp.contains(key)) {
            getValueImpl(sp, key)
        } else {
            null
        }
    }

    protected abstract fun getValueImpl(sp: SharedPreferences, key: String): T

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: T?) {
        if (value == null) {
            editor.remove(key)
        } else {
            putValueImpl(editor, key, value)
        }
    }

    protected abstract fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: T)
}

//<editor-fold defaultstate="collapsed" desc="Sp默认的各个支持直接存取的字段类型">
class StringNullableDelegate(key: String? = null) : AbsNullableSpDelegate<String>(key) {
    override fun getValueImpl(sp: SharedPreferences, key: String): String = sp.getString(key, "")!!

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: String) {
        editor.putString(key, value)
    }
}

class StringSetNullableDelegate(key: String? = null) : AbsNullableSpDelegate<Set<String>>(key) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Set<String> =
        sp.getStringSet(key, emptySet())!!

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Set<String>) {
        editor.putStringSet(key, value)
    }
}

class IntNullableDelegate(key: String? = null) : AbsNullableSpDelegate<Int>(key) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Int = sp.getInt(key, 0)

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Int) {
        editor.putInt(key, value)
    }
}

class LongNullableDelegate(key: String? = null) : AbsNullableSpDelegate<Long>(key) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Long = sp.getLong(key, 0)

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Long) {
        editor.putLong(key, value)
    }
}

class BooleanNullableDelegate(key: String? = null) : AbsNullableSpDelegate<Boolean>(key) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Boolean =
        sp.getBoolean(key, false)

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Boolean) {
        editor.putBoolean(key, value)
    }
}

class FloatNullableDelegate(key: String? = null) : AbsNullableSpDelegate<Float>(key) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Float = sp.getFloat(key, 0f)

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Float) {
        editor.putFloat(key, value)
    }
}
//</editor-fold>
