package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import io.github.chenfei0928.collection.mapToIntArray
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultValue
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate

class IntArraySpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
constructor(
    saver: AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, String?>,
) : SpConvert<SpSaver, Sp, Ed, String?, IntArray>(
    saver, PreferenceType.NoSupportPreferenceDataStore
) {

    constructor(key: String? = null) : this(StringDelegate(key))

    override fun onRead(value: String): IntArray =
        value.split(",")
            .mapToIntArray { it.toIntOrNull() ?: -1 }

    override fun onSave(value: IntArray): String {
        return value.joinToString(",")
    }

    companion object {
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> nonnull(
            defaultValue: IntArray = intArrayOf(), key: String? = null
        ): AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, IntArray> =
            IntArraySpConvert<SpSaver, Sp, Ed>(key).defaultValue(defaultValue)
    }
}
