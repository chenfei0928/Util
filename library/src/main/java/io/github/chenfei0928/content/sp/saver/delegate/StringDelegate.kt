package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.PreferenceType

class StringDelegate(
    key: String? = null,
) : AbsSpAccessDefaultValueDelegate<String?>(key, PreferenceType.Native.STRING, null) {
    override fun getValueImpl(sp: SharedPreferences, key: String): String? =
        sp.getString(key, null)

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: String) {
        editor.putString(key, value)
    }
}
