package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType

class StringSetDelegate<SpSaver : AbsSpSaver<SpSaver>>(
    key: String? = null,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Set<String>?>(
    key, PreferenceType.Native.STRING_SET, null
) {
    override fun getValueImpl(sp: SharedPreferences, key: String): Set<String>? =
        sp.getStringSet(key, null)

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: Set<String>) {
        editor.putStringSet(key, value)
    }
}
