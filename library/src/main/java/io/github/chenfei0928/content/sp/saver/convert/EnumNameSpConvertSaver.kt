package io.github.chenfei0928.content.sp.saver.convert

import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate

class EnumNameSpConvertSaver<E : Enum<E>>(
    eClass: Class<E>,
    private val enumValues: Array<E>,
    saver: AbsSpSaver.AbsSpDelegate<String?>,
) : SpConvertSaver<String?, E?>(saver, PreferenceType.EnumNameString(eClass, enumValues)) {

    constructor(
        eClass: Class<E>,
        enumValues: Array<E> = eClass.enumConstants as Array<E>,
        key: String? = null,
    ) : this(eClass, enumValues, StringDelegate(key))

    override fun onRead(value: String?): E? {
        return enumValues.find { value == it.name }
    }

    override fun onSave(value: E?): String? {
        return value?.name
    }

    companion object {
        inline operator fun <reified E : Enum<E>> invoke(
            key: String? = null
        ) = EnumNameSpConvertSaver(E::class.java, enumValues<E>(), key)
    }
}
