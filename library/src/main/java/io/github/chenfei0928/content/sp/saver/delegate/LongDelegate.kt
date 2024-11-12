package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences

class LongDelegate(
    key: String? = null, defaultValue: Long = 0L
) : AbsDefaultValueSpDelegate<Long>(key, defaultValue) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Long =
        sp.getLong(key, defaultValue)

    override fun putValueImpl(editor: SharedPreferences.Editor, key: String, value: Long) {
        editor.putLong(key, value)
    }
}
