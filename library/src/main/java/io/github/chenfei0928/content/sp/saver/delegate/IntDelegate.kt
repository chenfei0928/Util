package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType

class IntDelegate<SpSaver : AbsSpSaver<SpSaver>>(
    key: String? = null, defaultValue: Int = 0,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Int>(
    key, PreferenceType.Native.INT, defaultValue
) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Int =
        sp.getInt(key, defaultValue)

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: Int) {
        editor.putInt(key, value)
    }
}
