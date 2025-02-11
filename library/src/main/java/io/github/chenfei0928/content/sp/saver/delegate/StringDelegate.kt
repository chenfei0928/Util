package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultValue
import io.github.chenfei0928.util.DependencyChecker

open class StringDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
constructor(
    key: String? = null,
    private val expireDurationInSecond: Int = MMKV.ExpireNever,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, String?>(
    key, PreferenceType.Native.STRING, null
) {
    override fun getValueImpl(sp: Sp, key: String): String? =
        sp.getString(key, null)

    override fun putValue(editor: Ed, key: String, value: String) {
        if (expireDurationInSecond <= 0 || !DependencyChecker.MMKV() || editor !is MMKV) {
            editor.putString(key, value)
        } else {
            editor.putString(key, value, expireDurationInSecond)
        }
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
