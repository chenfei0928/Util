package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType

class BooleanDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
constructor(
    key: String? = null, defaultValue: Boolean = false,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, Boolean>(
    key, PreferenceType.Native.BOOLEAN, defaultValue
) {
    override fun getValueImpl(sp: Sp, key: String): Boolean =
        sp.getBoolean(key, defaultValue)

    override fun putValue(editor: Ed, key: String, value: Boolean) {
        editor.putBoolean(key, value)
    }
}
