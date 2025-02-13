package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import androidx.annotation.IntRange
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.util.DependencyChecker

class FloatDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
private constructor(
    key: String? = null,
    defaultValue: Float = 0f,
    @IntRange(from = 0) private val expireDurationInSecond: Int = MMKV.ExpireNever,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, Float>(
    key, PreferenceType.Native.FLOAT, defaultValue
) {
    override fun getValueImpl(sp: Sp, key: String): Float =
        sp.getFloat(key, defaultValue)

    override fun putValue(editor: Ed, key: String, value: Float) {
        if (expireDurationInSecond <= MMKV.ExpireNever || !DependencyChecker.MMKV() || editor !is MMKV) {
            editor.putFloat(key, value)
        } else {
            editor.putFloat(key, value, expireDurationInSecond)
        }
    }

    companion object {
        private var defaultInstance: FloatDelegate<*, *, *>? = null

        operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> invoke(
            key: String? = null,
            defaultValue: Float = 0f,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): FloatDelegate<SpSaver, Sp, Ed> {
            return if (!key.isNullOrEmpty() || defaultValue != 0f || expireDurationInSecond > 0) {
                FloatDelegate<SpSaver, Sp, Ed>(key, defaultValue, expireDurationInSecond)
            } else {
                @Suppress("UNCHECKED_CAST")
                defaultInstance as? FloatDelegate<SpSaver, Sp, Ed>
                    ?: FloatDelegate<SpSaver, Sp, Ed>().also { defaultInstance = it }
            }
        }
    }
}
