package io.github.chenfei0928.content.sp.saver.delegate

import androidx.annotation.IntRange
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType

/**
 * @author chenf()
 * @date 2025-02-11 14:21
 */
class ByteArrayDelegate<SpSaver : AbsSpSaver<SpSaver, Sp, Sp>, Sp : MMKV>
private constructor(
    key: String? = null,
    @IntRange(from = 0) private val expireDurationInSecond: Int = MMKV.ExpireNever,
    defaultValue: ByteArray? = null,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Sp, ByteArray?>(
    key, PreferenceType.NoSupportPreferenceDataStore, defaultValue
) {
    override fun getValueImpl(sp: Sp, key: String): ByteArray {
        return sp.getBytes(key, defaultValue)
    }

    override fun putValue(editor: Sp, key: String, value: ByteArray) {
        if (expireDurationInSecond == MMKV.ExpireNever) {
            editor.putBytes(key, value)
        } else {
            editor.putBytes(key, value, expireDurationInSecond)
        }
    }

    companion object {
        private var defaultInstance: ByteArrayDelegate<*, *>? = null
        private var defaultNonnullInstance: ByteArrayDelegate<*, *>? = null

        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Sp>, Sp : MMKV> nonNull(
            defaultValue: ByteArray = byteArrayOf(),
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, ByteArray> {
            @Suppress("UNCHECKED_CAST")
            return if (!key.isNullOrEmpty() || defaultValue.isNotEmpty() || expireDurationInSecond > 0) {
                ByteArrayDelegate<SpSaver, Sp>(key, expireDurationInSecond, defaultValue)
            } else {
                defaultNonnullInstance ?: ByteArrayDelegate<SpSaver, Sp>(
                    key, expireDurationInSecond, defaultValue
                ).also { defaultNonnullInstance = it }
            } as AbsSpSaver.Delegate<SpSaver, ByteArray>
        }

        operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Sp>, Sp : MMKV> invoke(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, ByteArray?> {
            return if (!key.isNullOrEmpty() || expireDurationInSecond > 0) {
                ByteArrayDelegate<SpSaver, Sp>(key, expireDurationInSecond)
            } else {
                @Suppress("UNCHECKED_CAST")
                defaultInstance as? ByteArrayDelegate<SpSaver, Sp>
                    ?: ByteArrayDelegate<SpSaver, Sp>().also { defaultInstance = it }
            }
        }
    }
}
