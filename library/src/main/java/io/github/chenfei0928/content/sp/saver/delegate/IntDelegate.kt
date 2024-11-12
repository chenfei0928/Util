package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences

class IntDelegate(
    key: String? = null, defaultValue: Int = 0
) : AbsDefaultValueSpDelegate<Int>(key, defaultValue) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Int =
        sp.getInt(key, defaultValue)

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Int) {
        editor.putInt(key, value)
    }
}
