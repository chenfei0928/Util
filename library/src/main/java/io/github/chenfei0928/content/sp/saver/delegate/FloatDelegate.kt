package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType

class FloatDelegate<SpSaver : AbsSpSaver<SpSaver>>(
    key: String? = null, defaultValue: Float = 0f,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Float>(
    key, PreferenceType.Native.FLOAT, defaultValue
) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Float =
        sp.getFloat(key, defaultValue)

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: Float) {
        editor.putFloat(key, value)
    }
}
