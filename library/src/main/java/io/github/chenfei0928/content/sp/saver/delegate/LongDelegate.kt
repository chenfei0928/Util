package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.PreferenceType

class LongDelegate(
    key: String? = null, defaultValue: Long = 0L,
) : AbsSpAccessDefaultValueDelegate<Long>(key, PreferenceType.Native.LONG, defaultValue) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Long =
        sp.getLong(key, defaultValue)

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: Long) {
        editor.putLong(key, value)
    }
}
