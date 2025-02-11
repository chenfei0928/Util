package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType

class FloatDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
constructor(
    key: String? = null, defaultValue: Float = 0f,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, Float>(
    key, PreferenceType.Native.FLOAT, defaultValue
) {
    override fun getValueImpl(sp: Sp, key: String): Float =
        sp.getFloat(key, defaultValue)

    override fun putValue(editor: Ed, key: String, value: Float) {
        editor.putFloat(key, value)
    }
}
