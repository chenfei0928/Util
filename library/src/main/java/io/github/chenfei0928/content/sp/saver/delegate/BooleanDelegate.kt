package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences

class BooleanDelegate(
    key: String? = null, defaultValue: Boolean = false
) : AbsDefaultValueSpDelegate<Boolean>(key, defaultValue) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Boolean =
        sp.getBoolean(key, defaultValue)

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Boolean) {
        editor.putBoolean(key, value)
    }
}
