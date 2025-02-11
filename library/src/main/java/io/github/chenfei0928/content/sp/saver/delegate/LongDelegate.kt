package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.util.DependencyChecker

class LongDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
constructor(
    key: String? = null, defaultValue: Long = 0L,
    private val expireDurationInSecond: Int = MMKV.ExpireNever,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, Long>(
    key, PreferenceType.Native.LONG, defaultValue
) {
    override fun getValueImpl(sp: Sp, key: String): Long =
        sp.getLong(key, defaultValue)

    override fun putValue(editor: Ed, key: String, value: Long) {
        if (expireDurationInSecond <= MMKV.ExpireNever || !DependencyChecker.MMKV() || editor !is MMKV) {
            editor.putLong(key, value)
        } else {
            editor.putLong(key, value, expireDurationInSecond)
        }
    }
}
