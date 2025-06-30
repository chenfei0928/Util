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
    defaultValue: ByteArray? = null,
    @param:IntRange(from = 0) private val expireDurationInSecond: Int = MMKV.ExpireNever,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Sp, ByteArray?>(
    key, spValueType, defaultValue
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
        internal val spValueType = PreferenceType.Struct<ByteArray>(ByteArray::class.java)
        private var defaultInstance: ByteArrayDelegate<*, *>? = null
        private var defaultNonnullInstance: ByteArrayDelegate<*, *>? = null

        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Sp>, Sp : MMKV> nonNull(
            defaultValue: ByteArray = byteArrayOf(),
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, ByteArray> {
            @Suppress("UNCHECKED_CAST")
            return if (!key.isNullOrEmpty() || defaultValue.isNotEmpty() || expireDurationInSecond > 0) {
                ByteArrayDelegate<SpSaver, Sp>(key, defaultValue, expireDurationInSecond)
            } else {
                defaultNonnullInstance ?: ByteArrayDelegate<SpSaver, Sp>(
                    key, defaultValue, expireDurationInSecond,
                ).also { defaultNonnullInstance = it }
            } as AbsSpSaver.Delegate<SpSaver, ByteArray>
        }

        operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Sp>, Sp : MMKV> invoke(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, ByteArray?> {
            return if (!key.isNullOrEmpty() || expireDurationInSecond > 0) {
                ByteArrayDelegate(key, null, expireDurationInSecond)
            } else {
                @Suppress("UNCHECKED_CAST")
                defaultInstance as? ByteArrayDelegate<SpSaver, Sp>
            } ?: ByteArrayDelegate<SpSaver, Sp>().also { defaultInstance = it }
        }
    }
}
