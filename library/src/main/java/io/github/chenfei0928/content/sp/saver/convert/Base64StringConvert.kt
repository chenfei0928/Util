package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
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
    saver: AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, String?>,
) : SpConvert<SpSaver, Sp, Ed, String?, ByteArray>(
    saver, PreferenceType.NoSupportPreferenceDataStore
) {

    constructor(key: String? = null) : this(StringDelegate<SpSaver, Sp, Ed>(key))

    override fun onRead(value: String): ByteArray {
        return value.toByteArray()
    }

    override fun onSave(value: ByteArray): String {
        return String(value)
    }
}
