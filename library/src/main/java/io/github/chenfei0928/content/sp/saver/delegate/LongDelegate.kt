package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType

class LongDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
constructor(
    key: String? = null, defaultValue: Long = 0L,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, Long>(
    key, PreferenceType.Native.LONG, defaultValue
) {
    override fun getValueImpl(sp: Sp, key: String): Long =
        sp.getLong(key, defaultValue)

    override fun putValue(editor: Ed, key: String, value: Long) {
        editor.putLong(key, value)
    }
}
