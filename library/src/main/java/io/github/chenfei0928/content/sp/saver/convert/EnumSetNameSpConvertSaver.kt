package io.github.chenfei0928.content.sp.saver.convert

import androidx.collection.ArraySet
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.StringSetDelegate

class EnumSetNameSpConvertSaver<E : Enum<E>>(
    eClass: Class<E>,
    private val enumValues: Array<E>,
    saver: AbsSpSaver.AbsSpDelegate<Set<String>?>,
    private val nameNotFoundDefaultValue: E? = null,
) : SpConvertSaver<Set<String>?, Set<E>?>(saver, EnumNameStringSet(eClass, enumValues)) {

    constructor(
        eClass: Class<E>,
        enumValues: Array<E> = eClass.enumConstants as Array<E>,
        key: String? = null,
        nameNotFoundDefaultValue: E? = null,
    ) : this(eClass, enumValues, StringSetDelegate(key), nameNotFoundDefaultValue)

    override fun onRead(value: Set<String>): Set<E> {
        return value.mapNotNullTo(ArraySet(value.size)) { item ->
            enumValues.find { enum -> item == enum.name } ?: nameNotFoundDefaultValue
        }
    }

    override fun onSave(value: Set<E>): Set<String> {
        return value.mapTo(ArraySet(value.size)) { it.name }
    }

    private class EnumNameStringSet<E : Enum<E>>(
        eClass: Class<E>, values: Array<E>,
    ) : PreferenceType.BaseEnumNameStringCollection<E, MutableSet<E>>(eClass, values) {
        override fun createCollection(size: Int): MutableSet<E> = ArraySet(size)
    }

    companion object {
        inline operator fun <reified E : Enum<E>> invoke(
            key: String? = null
        ) = EnumSetNameSpConvertSaver(E::class.java, enumValues<E>(), key)
    }
}
