package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import androidx.annotation.IntRange
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.util.DependencyChecker

class LongDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
private constructor(
    key: String? = null,
    defaultValue: Long = 0L,
    @IntRange(from = 0) private val expireDurationInSecond: Int = MMKV.ExpireNever,
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

    companion object {
        private var defaultInstance: LongDelegate<*, *, *>? = null

        operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> invoke(
            key: String? = null,
            defaultValue: Long = 0L,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): LongDelegate<SpSaver, Sp, Ed> {
            return if (!key.isNullOrEmpty() || defaultValue != 0L || expireDurationInSecond > 0) {
                LongDelegate<SpSaver, Sp, Ed>(key, defaultValue, expireDurationInSecond)
            } else {
                @Suppress("UNCHECKED_CAST")
                defaultInstance as? LongDelegate<SpSaver, Sp, Ed>
                    ?: LongDelegate<SpSaver, Sp, Ed>().also { defaultInstance = it }
            }
        }
    }
}
