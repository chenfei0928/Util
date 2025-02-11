package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultValue

open class StringDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
constructor(
    key: String? = null
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, String?>(
    key, PreferenceType.Native.STRING, null
) {
    override fun getValueImpl(sp: Sp, key: String): String? =
        sp.getString(key, null)

    override fun putValue(editor: Ed, key: String, value: String) {
        editor.putString(key, value)
    }

    companion object {
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> nonNull(
            defaultValue: String = "", key: String? = null,
        ): AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, String> =
            StringDelegate<SpSaver, Sp, Ed>(key).defaultValue(defaultValue)
    }
}
