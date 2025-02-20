package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import androidx.annotation.IntRange
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate

class EnumNameSpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        E : Enum<E>>
constructor(
    saver: AbsSpSaver.Delegate<SpSaver, String?>,
    override val spValueType: PreferenceType.EnumNameString<E>,
    override val defaultValue: E?,
) : BaseSpConvert<SpSaver, Sp, Ed, String?, E?>(saver), AbsSpSaver.DefaultValue<E?> {

    constructor(
        eClass: Class<E>,
        enumValues: Array<E> = eClass.enumConstants as Array<E>,
        key: String? = null,
        @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        defaultValue: E? = null,
    ) : this(
        StringDelegate(key, expireDurationInSecond),
        PreferenceType.EnumNameString(eClass, enumValues),
        defaultValue
    )

    override fun onRead(value: String): E = spValueType.forName(value, defaultValue)

    override fun onSave(value: E): String = value.name

    companion object {
        inline operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified E : Enum<E>> invoke(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, E?> = EnumNameSpConvert<SpSaver, Sp, Ed, E>(
            saver = StringDelegate(key, expireDurationInSecond),
            spValueType = PreferenceType.EnumNameString(),
            defaultValue = null,
        )

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified E : Enum<E>> nonnull(
            defaultValue: E,
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, E> {
            @Suppress("UNCHECKED_CAST")
            return EnumNameSpConvert<SpSaver, Sp, Ed, E>(
                saver = StringDelegate(key, expireDurationInSecond),
                spValueType = PreferenceType.EnumNameString(),
                defaultValue = defaultValue,
            ) as AbsSpSaver.Delegate<SpSaver, E>
        }
    }
}
