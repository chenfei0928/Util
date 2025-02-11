package io.github.chenfei0928.content.sp.saver.delegate

import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultValue

/**
 * @author chenf()
 * @date 2025-02-11 14:21
 */
class ByteArrayDelegate<SpSaver : AbsSpSaver<SpSaver, Sp, Sp>, Sp : MMKV>(
    key: String? = null,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Sp, ByteArray?>(
    key, PreferenceType.NoSupportPreferenceDataStore, null
) {
    override fun getValueImpl(sp: Sp, key: String): ByteArray {
        return sp.getBytes(key, defaultValue)
    }

    override fun putValue(editor: Sp, key: String, value: ByteArray) {
        editor.putBytes(key, value)
    }

    companion object {
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Sp>, Sp : MMKV> nonNull(
            defaultValue: ByteArray = byteArrayOf(), key: String? = null,
        ): AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Sp, ByteArray> =
            ByteArrayDelegate<SpSaver, Sp>(key).defaultValue(defaultValue)
    }
}
