package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import androidx.annotation.IntRange
import androidx.collection.ArraySet
import com.google.protobuf.ProtocolMessageEnum
import com.google.protobuf.protobufEnumUnrecognized
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
    private val spValueTypeImpl: PreferenceType.BaseEnumNameStringCollection<E, MutableSet<E>>,
    private val nameNotFoundDefaultValue: E? = null,
    override val defaultValue: Set<E>?,
) : BaseSpConvert<SpSaver, Sp, Ed, Set<String?>?, Set<E?>?>(saver),
    AbsSpSaver.DefaultValue<Set<E?>?> {
    override val spValueType: PreferenceType<Set<E?>> = spValueTypeImpl as PreferenceType<Set<E?>>

    constructor(
        eClass: Class<E>,
        nameNotFoundDefaultValue: E?,
        enumValues: Array<E> = eClass.enumConstants as Array<E>,
        key: String? = null,
        @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        defaultValue: Set<E>? = null,
    ) : this(
        saver = StringSetDelegate(key, expireDurationInSecond),
        spValueTypeImpl = EnumNameStringSet(PreferenceType.EnumNameString(eClass, enumValues)),
        nameNotFoundDefaultValue = nameNotFoundDefaultValue,
        defaultValue = defaultValue
    )

    override fun onRead(value: Set<String?>): Set<E?> {
        return spValueTypeImpl.forNames(value, false, nameNotFoundDefaultValue)
    }

    override fun onSave(value: Set<E?>): Set<String?> {
        @Suppress("kotlin:S6531")
        return spValueTypeImpl.toNames(value, false) as Set<String?>
    }

    override fun toString(): String = "EnumSetNameSpConvert(saver=$saver, spValueType=$spValueType)"

    class EnumNameStringSet<E : Enum<E>>(
        type: PreferenceType.EnumNameString<E>,
    ) : PreferenceType.BaseEnumNameStringCollection<E, MutableSet<E>>(type) {
        override fun <MC : MutableCollection<E>> createCollection(size: Int): MC {
            @Suppress("UNCHECKED_CAST")
            return ArraySet<E>(size) as MC
        }

        companion object {
            inline operator fun <reified E : Enum<E>> invoke() =
                EnumNameStringSet(PreferenceType.EnumNameString<E>())
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
                spValueTypeImpl = EnumNameStringSet(),
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
                spValueTypeImpl = EnumNameStringSet(),
                nameNotFoundDefaultValue = nameNotFoundDefaultValue,
                defaultValue = defaultValue
            ) as AbsSpSaver.Delegate<SpSaver, Set<E>>
        }

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified E> protobuf(
            nameNotFoundDefaultValue: E = protobufEnumUnrecognized<E>(),
            defaultValue: Set<E> = emptySet(),
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, Set<E>> where E : Enum<E>, E : ProtocolMessageEnum {
            @Suppress("UNCHECKED_CAST")
            return EnumSetNameSpConvert<SpSaver, Sp, Ed, E>(
                saver = StringSetDelegate(key, expireDurationInSecond),
                spValueTypeImpl = EnumNameStringSet(PreferenceType.EnumNameString.ProtobufEnumNumber()),
                nameNotFoundDefaultValue = nameNotFoundDefaultValue,
                defaultValue = defaultValue
            ) as AbsSpSaver.Delegate<SpSaver, Set<E>>
        }
    }
}
