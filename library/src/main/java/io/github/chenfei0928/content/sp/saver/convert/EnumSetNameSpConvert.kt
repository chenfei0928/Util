package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import androidx.collection.ArraySet
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultValue
import io.github.chenfei0928.content.sp.saver.delegate.StringSetDelegate

class EnumSetNameSpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        E : Enum<E>>
constructor(
    eClass: Class<E>,
    private val enumValues: Array<E>,
    saver: AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, Set<String>?>,
    private val nameNotFoundDefaultValue: E? = null,
) : SpConvert<SpSaver, Sp, Ed, Set<String>?, Set<E>?>(
    saver,
    EnumNameStringSet(eClass, enumValues)
) {

    constructor(
        eClass: Class<E>,
        enumValues: Array<E> = eClass.enumConstants as Array<E>,
        key: String? = null,
        nameNotFoundDefaultValue: E? = null,
    ) : this(
        eClass,
        enumValues,
        StringSetDelegate(key),
        nameNotFoundDefaultValue
    )

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
        inline operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified E : Enum<E>> invoke(
            key: String? = null
        ) = EnumSetNameSpConvert<SpSaver, Sp, Ed, E>(E::class.java, enumValues<E>(), key)

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified E : Enum<E>> nonnull(
            defaultValue: Set<E> = emptySet(), key: String? = null
        ): AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, Set<E>> =
            EnumSetNameSpConvert<SpSaver, Sp, Ed, E>(
                E::class.java, enumValues<E>(), key
            ).defaultValue(defaultValue)
    }
}
