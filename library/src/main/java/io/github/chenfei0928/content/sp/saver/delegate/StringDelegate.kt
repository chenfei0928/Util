package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences

class StringDelegate(
    key: String? = null
) : AbsDefaultValueSpDelegate<String?>(key, null) {
    override fun getValueImpl(sp: SharedPreferences, key: String): String? =
        sp.getString(key, null)

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: String?) {
        editor.putString(key, value)
    }
}
