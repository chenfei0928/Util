package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
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
) : BaseSpConvert<SpSaver, Sp, Ed, String?, ByteArray>(
    saver, PreferenceType.NoSupportPreferenceDataStore
) {

    private constructor(
        key: String? = null,
        expireDurationInSecond: Int = MMKV.ExpireNever,
    ) : this(StringDelegate<SpSaver, Sp, Ed>(key, expireDurationInSecond))

    override fun onRead(value: String): ByteArray = value.toByteArray()
    override fun onSave(value: ByteArray): String = String(value)

    companion object {
        private var defaultInstance: Base64StringConvert<*, *, *>? = null

        operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> invoke(
            key: String? = null, expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, ByteArray?> {
            @Suppress("UNCHECKED_CAST")
            return if (!key.isNullOrEmpty() || expireDurationInSecond > 0)
                Base64StringConvert<SpSaver, Sp, Ed>(key, expireDurationInSecond)
            else defaultInstance as? Base64StringConvert<SpSaver, Sp, Ed>
                ?: Base64StringConvert<SpSaver, Sp, Ed>().also { defaultInstance = it }
        }
    }
}
