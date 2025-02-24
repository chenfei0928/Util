package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import androidx.annotation.IntRange
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.util.DependencyChecker

class BooleanDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
private constructor(
    key: String? = null,
    defaultValue: Boolean = false,
    @IntRange(from = 0) private val expireDurationInSecond: Int = MMKV.ExpireNever,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, Boolean>(
    key, PreferenceType.Native.BOOLEAN, defaultValue
) {
    override fun getValueImpl(sp: Sp, key: String): Boolean =
        sp.getBoolean(key, defaultValue)

    override fun putValue(editor: Ed, key: String, value: Boolean) {
        if (expireDurationInSecond <= MMKV.ExpireNever || !DependencyChecker.mmkv || editor !is MMKV) {
            editor.putBoolean(key, value)
        } else {
            editor.putBoolean(key, value, expireDurationInSecond)
        }
    }

    companion object {
        private var defaultInstance: BooleanDelegate<*, *, *>? = null

        operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> invoke(
            key: String? = null,
            defaultValue: Boolean = false,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): BooleanDelegate<SpSaver, Sp, Ed> {
            return if (!key.isNullOrEmpty() || defaultValue != false || expireDurationInSecond > 0) {
                BooleanDelegate<SpSaver, Sp, Ed>(key, defaultValue, expireDurationInSecond)
            } else {
                @Suppress("UNCHECKED_CAST")
                defaultInstance as? BooleanDelegate<SpSaver, Sp, Ed>
            } ?: BooleanDelegate<SpSaver, Sp, Ed>().also { defaultInstance = it }
        }
    }
}
