package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType

class BooleanDelegate<SpSaver : AbsSpSaver<SpSaver>>(
    key: String? = null, defaultValue: Boolean = false,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Boolean>(
    key, PreferenceType.Native.BOOLEAN, defaultValue
) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Boolean =
        sp.getBoolean(key, defaultValue)

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: Boolean) {
        editor.putBoolean(key, value)
    }
}
