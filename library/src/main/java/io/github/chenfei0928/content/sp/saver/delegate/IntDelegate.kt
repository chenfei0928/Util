package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import androidx.annotation.IntRange
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.util.DependencyChecker

class IntDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
private constructor(
    key: String? = null,
    defaultValue: Int = 0,
    @IntRange(from = 0) private val expireDurationInSecond: Int = MMKV.ExpireNever,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, Int>(
    key, PreferenceType.Native.INT, defaultValue
) {
    override fun getValueImpl(sp: Sp, key: String): Int =
        sp.getInt(key, defaultValue)

    override fun putValue(editor: Ed, key: String, value: Int) {
        if (expireDurationInSecond <= MMKV.ExpireNever || !DependencyChecker.MMKV() || editor !is MMKV) {
            editor.putInt(key, value)
        } else {
            editor.putInt(key, value, expireDurationInSecond)
        }
    }

    companion object {
        private var defaultInstance: IntDelegate<*, *, *>? = null

        operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> invoke(
            key: String? = null,
            defaultValue: Int = 0,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): IntDelegate<SpSaver, Sp, Ed> {
            return if (!key.isNullOrEmpty() || defaultValue != 0 || expireDurationInSecond > 0) {
                IntDelegate<SpSaver, Sp, Ed>(key, defaultValue, expireDurationInSecond)
            } else {
                @Suppress("UNCHECKED_CAST")
                defaultInstance as? IntDelegate<SpSaver, Sp, Ed>
            } ?: IntDelegate<SpSaver, Sp, Ed>().also { defaultInstance = it }
        }
    }
}
