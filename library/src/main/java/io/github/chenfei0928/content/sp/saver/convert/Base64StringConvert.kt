package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import androidx.annotation.IntRange
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.ByteArrayDelegate
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate

/**
 * @author chenf()
 * @date 2025-02-11 14:45
 */
class Base64StringConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
constructor(
    saver: AbsSpSaver.Delegate<SpSaver, String?>,
) : BaseSpConvert<SpSaver, Sp, Ed, String?, ByteArray>(saver) {
    override val spValueType: PreferenceType = ByteArrayDelegate.spValueType
    override fun onRead(value: String): ByteArray = value.toByteArray()
    override fun onSave(value: ByteArray): String = String(value)
    override fun toString(): String = "Base64StringConvert(saver=$saver)"

    companion object {
        private var defaultInstance: Base64StringConvert<*, *, *>? = null

        operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> invoke(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, ByteArray?> {
            return if (!key.isNullOrEmpty() || expireDurationInSecond > 0) {
                Base64StringConvert(StringDelegate(key, expireDurationInSecond))
            } else {
                @Suppress("UNCHECKED_CAST")
                defaultInstance as? Base64StringConvert<SpSaver, Sp, Ed>
            } ?: Base64StringConvert<SpSaver, Sp, Ed>(
                StringDelegate(key, expireDurationInSecond)
            ).also { defaultInstance = it }
        }
    }
}
