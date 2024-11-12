package io.github.chenfei0928.content.sp.saver.convert

import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate

class EnumNameSpConvertSaver<E : Enum<E>>(
    private val enumValues: Array<E>,
    saver: AbsSpSaver.AbsSpDelegate0<String?>
) : SpConvertSaver<String?, E?>(saver) {

    constructor(
        enumValues: Array<E>, key: String? = null
    ) : this(enumValues, StringDelegate(key))

    override fun onRead(value: String?): E? {
        return enumValues.find { value == it.name }
    }

    override fun onSave(value: E?): String? {
        return value?.name
    }

    companion object {
        inline fun <reified E : Enum<E>> EnumNameSpConvertSaver(
            key: String? = null
        ) = EnumNameSpConvertSaver(enumValues<E>(), key)
    }
}
