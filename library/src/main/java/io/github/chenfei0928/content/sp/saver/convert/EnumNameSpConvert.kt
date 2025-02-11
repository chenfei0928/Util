package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultValue
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate

class EnumNameSpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        E : Enum<E>>
constructor(
    eClass: Class<E>,
    private val enumValues: Array<E>,
    saver: AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, String?>,
    private val nameNotFoundDefaultValue: E? = null,
) : SpConvert<SpSaver, Sp, Ed, String?, E?>(
    saver, PreferenceType.EnumNameString(eClass, enumValues)
) {

    constructor(
        eClass: Class<E>,
        enumValues: Array<E> = eClass.enumConstants as Array<E>,
        key: String? = null,
        nameNotFoundDefaultValue: E? = null,
    ) : this(eClass, enumValues, StringDelegate(key), nameNotFoundDefaultValue)

    override fun onRead(value: String): E? {
        return enumValues.find { value == it.name } ?: nameNotFoundDefaultValue
    }

    override fun onSave(value: E): String {
        return value.name
    }

    companion object {
        inline operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified E : Enum<E>> invoke(
            key: String? = null
        ) = EnumNameSpConvert<SpSaver, Sp, Ed, E>(E::class.java, enumValues<E>(), key)

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified E : Enum<E>> nonnull(
            defaultValue: E, key: String? = null
        ): AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, E> =
            EnumNameSpConvert<SpSaver, Sp, Ed, E>(
                E::class.java, enumValues<E>(), key
            ).defaultValue(defaultValue)
    }
}
