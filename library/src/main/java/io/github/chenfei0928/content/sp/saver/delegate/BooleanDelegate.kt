package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.util.DependencyChecker

class BooleanDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
constructor(
    key: String? = null, defaultValue: Boolean = false,
    private val expireDurationInSecond: Int = MMKV.ExpireNever,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, Boolean>(
    key, PreferenceType.Native.BOOLEAN, defaultValue
) {
    override fun getValueImpl(sp: Sp, key: String): Boolean =
        sp.getBoolean(key, defaultValue)

    override fun putValue(editor: Ed, key: String, value: Boolean) {
        if (expireDurationInSecond <= MMKV.ExpireNever || !DependencyChecker.MMKV() || editor !is MMKV) {
            editor.putBoolean(key, value)
        } else {
            editor.putBoolean(key, value, expireDurationInSecond)
        }
    }
}
