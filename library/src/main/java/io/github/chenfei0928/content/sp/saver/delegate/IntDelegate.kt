package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.PreferenceType

class IntDelegate(
    key: String? = null, defaultValue: Int = 0,
) : AbsSpAccessDefaultValueDelegate<Int>(key, PreferenceType.Native.INT, defaultValue) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Int =
        sp.getInt(key, defaultValue)

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: Int) {
        editor.putInt(key, value)
    }
}
