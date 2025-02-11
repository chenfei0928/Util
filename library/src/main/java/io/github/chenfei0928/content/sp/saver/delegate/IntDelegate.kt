package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType

class IntDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
constructor(
    key: String? = null, defaultValue: Int = 0,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, Int>(
    key, PreferenceType.Native.INT, defaultValue
) {
    override fun getValueImpl(sp: Sp, key: String): Int =
        sp.getInt(key, defaultValue)

    override fun putValue(editor: Ed, key: String, value: Int) {
        editor.putInt(key, value)
    }
}
