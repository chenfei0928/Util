package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import androidx.collection.ArraySet
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.StringSetDelegate

class EnumSetNameSpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        E : Enum<E>>
constructor(
    eClass: Class<E>,
    private val nameNotFoundDefaultValue: E,
    private val enumValues: Array<E>,
    saver: AbsSpSaver.Delegate<SpSaver, Set<String?>?>,
    override val defaultValue: Set<E>? = null,
) : BaseSpConvert<SpSaver, Sp, Ed, Set<String?>?, Set<E?>?>(
    saver, EnumNameStringSet(eClass, enumValues)
), AbsSpSaver.DefaultValue<Set<E?>?> {

    constructor(
        eClass: Class<E>,
        nameNotFoundDefaultValue: E,
        enumValues: Array<E> = eClass.enumConstants as Array<E>,
        key: String? = null,
        expireDurationInSecond: Int = MMKV.ExpireNever,
        defaultValue: Set<E>? = null,
    ) : this(
        eClass,
        nameNotFoundDefaultValue,
        enumValues,
        StringSetDelegate(key, expireDurationInSecond),
        defaultValue
    )

    override fun onRead(value: Set<String?>): Set<E?> {
        return value.mapNotNullTo(ArraySet(value.size)) { item ->
            enumValues.find { enum -> item == enum.name } ?: nameNotFoundDefaultValue
        }
    }

    override fun onSave(value: Set<E?>): Set<String?> {
        return value.mapTo(ArraySet(value.size)) { it?.name }
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
            nameNotFoundDefaultValue: E,
            key: String? = null,
            expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, Set<E?>?> {
            return EnumSetNameSpConvert<SpSaver, Sp, Ed, E>(
                E::class.java,
                nameNotFoundDefaultValue,
                enumValues<E>(),
                key,
                expireDurationInSecond
            )
        }

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified E : Enum<E>> nonnull(
            nameNotFoundDefaultValue: E,
            defaultValue: Set<E> = emptySet(),
            key: String? = null,
            expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, Set<E>> {
            @Suppress("UNCHECKED_CAST")
            return EnumSetNameSpConvert<SpSaver, Sp, Ed, E>(
                E::class.java,
                nameNotFoundDefaultValue,
                enumValues<E>(),
                key,
                expireDurationInSecond,
                defaultValue
            ) as AbsSpSaver.Delegate<SpSaver, Set<E>>
        }
    }
}
