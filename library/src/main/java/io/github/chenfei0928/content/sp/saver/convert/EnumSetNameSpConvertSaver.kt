package io.github.chenfei0928.content.sp.saver.convert

import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.delegate.StringSetDelegate

class EnumSetNameSpConvertSaver<E : Enum<E>>(
    private val enumValues: Array<E>,
    saver: AbsSpSaver.AbsSpDelegate0<Set<String>?>
) : SpConvertSaver<Set<String>?, Set<E>?>(saver) {

    constructor(
        enumValues: Array<E>, key: String? = null
    ) : this(enumValues, StringSetDelegate(key))

    override fun onRead(value: Set<String>?): Set<E>? {
        return value?.mapNotNullTo(HashSet()) { item ->
            enumValues.find { enum ->
                item == enum.name
            }
        }
    }

    override fun onSave(value: Set<E>?): Set<String>? {
        return value?.mapTo(HashSet()) { it.name }
    }

    companion object {
        inline operator fun <reified E : Enum<E>> invoke(
            key: String? = null
        ) = EnumSetNameSpConvertSaver(enumValues<E>(), key)
    }
}
