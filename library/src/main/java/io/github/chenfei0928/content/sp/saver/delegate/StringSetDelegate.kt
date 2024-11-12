package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences

class StringSetDelegate(
    key: String? = null
) : AbsDefaultValueSpDelegate<Set<String>?>(key, null) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Set<String>? =
        sp.getStringSet(key, null)

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Set<String>?) {
        editor.putStringSet(key, value)
    }
}
