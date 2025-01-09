package io.github.chenfei0928.content.sp.saver.convert

import io.github.chenfei0928.collection.mapToIntArray
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate

class IntArraySpConvertSaver(
    saver: AbsSpSaver.AbsSpDelegate<String?>,
) : SpConvertSaver<String?, IntArray?>(saver, PreferenceType.NoSupportPreferenceDataStore) {

    constructor(key: String? = null) : this(StringDelegate(key))

    override fun onRead(value: String?): IntArray? =
        value?.split(",")
            ?.mapToIntArray { it.toIntOrNull() ?: -1 }

    override fun onSave(value: IntArray?): String? {
        return value?.joinToString(",")
    }
}
