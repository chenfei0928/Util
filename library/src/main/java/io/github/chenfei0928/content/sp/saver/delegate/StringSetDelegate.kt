package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultValue
import io.github.chenfei0928.util.DependencyChecker

class StringSetDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
constructor(
    key: String? = null,
    private val expireDurationInSecond: Int = MMKV.ExpireNever,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, Set<String>?>(
    key, PreferenceType.Native.STRING_SET, null
) {
    override fun getValueImpl(sp: Sp, key: String): Set<String>? =
        sp.getStringSet(key, null)

    override fun putValue(editor: Ed, key: String, value: Set<String>) {
        if (expireDurationInSecond <= MMKV.ExpireNever || !DependencyChecker.MMKV() || editor !is MMKV) {
            editor.putStringSet(key, value)
        } else {
            editor.putStringSet(key, value, expireDurationInSecond)
        }
    }

    companion object {
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> nonNull(
            defaultValue: Set<String> = emptySet(), key: String? = null,
        ): AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, Set<String>> =
            StringSetDelegate<SpSaver, Sp, Ed>(key).defaultValue(defaultValue)
    }
}
