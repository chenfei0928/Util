package io.github.chenfei0928.content.sp.saver.convert

import androidx.collection.ArraySet
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.StringSetDelegate

class EnumSetNameSpConvertSaver<E : Enum<E>>(
    private val enumValues: Array<E>,
    saver: AbsSpSaver.AbsSpDelegate<Set<String>?>,
) : SpConvertSaver<Set<String>?, Set<E>?>(saver, EnumNameStringSet(enumValues)) {

    constructor(
        enumValues: Array<E>, key: String? = null
    ) : this(enumValues, StringSetDelegate(key))

    override fun onRead(value: Set<String>?): Set<E>? {
        return value?.mapNotNullTo(ArraySet(value.size)) { item ->
            enumValues.find { enum ->
                item == enum.name
            }
        }
    }

    override fun onSave(value: Set<E>?): Set<String>? {
        return value?.mapTo(ArraySet(value.size)) { it.name }
    }

    private class EnumNameStringSet<E : Enum<E>>(
        values: Array<E>,
    ) : PreferenceType.BaseEnumNameStringCollection<E, MutableSet<E>>(values) {
        override fun createCollection(size: Int): MutableSet<E> = ArraySet(size)
    }

    companion object {
        inline operator fun <reified E : Enum<E>> invoke(
            key: String? = null
        ) = EnumSetNameSpConvertSaver(enumValues<E>(), key)
    }
}
