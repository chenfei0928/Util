package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import androidx.annotation.IntRange
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
    saver: AbsSpSaver.Delegate<SpSaver, Set<String?>?>,
    override val spValueType: PreferenceType.BaseEnumNameStringCollection<E, out Set<E>>,
    private val nameNotFoundDefaultValue: E? = null,
    override val defaultValue: Set<E>?,
) : BaseSpConvert<SpSaver, Sp, Ed, Set<String?>?, Set<E?>?>(saver),
    AbsSpSaver.DefaultValue<Set<E?>?> {

    constructor(
        eClass: Class<E>,
        nameNotFoundDefaultValue: E?,
        enumValues: Array<E> = eClass.enumConstants as Array<E>,
        key: String? = null,
        @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        defaultValue: Set<E>? = null,
    ) : this(
        saver = StringSetDelegate(key, expireDurationInSecond),
        spValueType = EnumNameStringSet(eClass, enumValues),
        nameNotFoundDefaultValue = nameNotFoundDefaultValue,
        defaultValue = defaultValue
    )

    override fun onRead(value: Set<String?>): Set<E?> {
        return value.mapNotNullTo(ArraySet(value.size)) { item ->
            spValueType.forNameOrNull(item, nameNotFoundDefaultValue)
        }
    }

    override fun onSave(value: Set<E?>): Set<String?> {
        return value.mapTo(ArraySet(value.size)) { it?.name }
    }

    override fun toString(): String = "EnumSetNameSpConvert(saver=$saver, spValueType=$spValueType)"

    class EnumNameStringSet<E : Enum<E>>(
        eClass: Class<E>, values: Array<E>,
    ) : PreferenceType.BaseEnumNameStringCollection<E, MutableSet<E>>(eClass, values) {
        override fun <MC : MutableCollection<E>> createCollection(size: Int): MC {
            @Suppress("UNCHECKED_CAST")
            return ArraySet<E>(size) as MC
        }

        companion object {
            inline operator fun <reified E : Enum<E>> invoke() =
                EnumNameStringSet(E::class.java, enumValues<E>())
        }
    }

    companion object {
        inline operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified E : Enum<E>> invoke(
            nameNotFoundDefaultValue: E? = null,
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, Set<E?>?> {
            return EnumSetNameSpConvert<SpSaver, Sp, Ed, E>(
                saver = StringSetDelegate(key, expireDurationInSecond),
                spValueType = EnumNameStringSet(),
                nameNotFoundDefaultValue = nameNotFoundDefaultValue,
                defaultValue = null,
            )
        }

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified E : Enum<E>> nonnull(
            nameNotFoundDefaultValue: E,
            defaultValue: Set<E> = emptySet(),
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, Set<E>> {
            @Suppress("UNCHECKED_CAST")
            return EnumSetNameSpConvert<SpSaver, Sp, Ed, E>(
                saver = StringSetDelegate(key, expireDurationInSecond),
                spValueType = EnumNameStringSet(),
                nameNotFoundDefaultValue = nameNotFoundDefaultValue,
                defaultValue = defaultValue
            ) as AbsSpSaver.Delegate<SpSaver, Set<E>>
        }
    }
}
