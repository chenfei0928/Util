package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import androidx.annotation.IntRange
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.util.DependencyChecker

open class StringDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
private constructor(
    key: String? = null,
    @IntRange(from = 0) private val expireDurationInSecond: Int = MMKV.ExpireNever,
    defaultValue: String? = null
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, String?>(
    key, PreferenceType.Native.STRING, defaultValue
) {
    override fun getValueImpl(sp: Sp, key: String): String =
        sp.getString(key, defaultValue)!!

    override fun putValue(editor: Ed, key: String, value: String) {
        if (expireDurationInSecond <= 0 || !DependencyChecker.MMKV() || editor !is MMKV) {
            editor.putString(key, value)
        } else {
            editor.putString(key, value, expireDurationInSecond)
        }
    }

    companion object {
        private var defaultInstance: StringDelegate<*, *, *>? = null
        private var defaultNonnullInstance: StringDelegate<*, *, *>? = null

        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> nonNull(
            defaultValue: String = "",
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, String> {
            @Suppress("UNCHECKED_CAST")
            return if (!key.isNullOrEmpty() || defaultValue.isNotEmpty() || expireDurationInSecond > 0) {
                StringDelegate<SpSaver, Sp, Ed>(key, expireDurationInSecond, defaultValue)
            } else {
                defaultNonnullInstance ?: StringDelegate<SpSaver, Sp, Ed>(
                    key, expireDurationInSecond, defaultValue
                ).also { defaultNonnullInstance = it }
            } as AbsSpSaver.Delegate<SpSaver, String>
        }

        operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> invoke(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, String?> {
            return if (!key.isNullOrEmpty() || expireDurationInSecond > 0) {
                StringDelegate<SpSaver, Sp, Ed>(key, expireDurationInSecond)
            } else {
                @Suppress("UNCHECKED_CAST")
                defaultInstance as? StringDelegate<SpSaver, Sp, Ed>
                    ?: StringDelegate<SpSaver, Sp, Ed>().also { defaultInstance = it }
            }
        }
    }
}
