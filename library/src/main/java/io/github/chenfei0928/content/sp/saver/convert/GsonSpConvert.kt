package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import androidx.annotation.IntRange
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate

abstract class GsonSpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        V>
protected constructor(
    saver: AbsSpSaver.Delegate<SpSaver, String?>,
    private val gson: Gson = io.github.chenfei0928.json.gson.gson,
    private val type: TypeToken<V>,
) : BaseSpConvert<SpSaver, Sp, Ed, String?, V?>(
    saver, PreferenceType.NoSupportPreferenceDataStore
), AbsSpSaver.DefaultValue<V?> {

    override fun onRead(value: String): V & Any = gson.fromJson<V & Any>(value, type)
    override fun onSave(value: V & Any): String = gson.toJson(value)

    class ValueDefaultValue<SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
            Sp : SharedPreferences,
            Ed : SharedPreferences.Editor,
            V>
    constructor(
        saver: AbsSpSaver.Delegate<SpSaver, String?>,
        gson: Gson = io.github.chenfei0928.json.gson.gson,
        type: TypeToken<V>,
        override val defaultValue: V? = null,
    ) : GsonSpConvert<SpSaver, Sp, Ed, V>(saver, gson, type)

    companion object {
        inline operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified V> invoke(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, V?> = ValueDefaultValue<SpSaver, Sp, Ed, V>(
            saver = StringDelegate(key, expireDurationInSecond),
            type = object : TypeToken<V>() {}
        )

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified V> nonnullByBlock(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
            noinline defaultValue: () -> V & Any
        ): AbsSpSaver.Delegate<SpSaver, V & Any> {
            @Suppress("UNCHECKED_CAST")
            return object : GsonSpConvert<SpSaver, Sp, Ed, V>(
                saver = StringDelegate(key, expireDurationInSecond),
                type = object : TypeToken<V>() {}
            ), AbsSpSaver.DefaultValue<V & Any> {
                override val defaultValue: V & Any by lazy(defaultValue)
            } as AbsSpSaver.Delegate<SpSaver, V & Any>
        }

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified V> nonnull(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
            defaultValue: V & Any
        ): AbsSpSaver.Delegate<SpSaver, V & Any> {
            @Suppress("UNCHECKED_CAST")
            return ValueDefaultValue<SpSaver, Sp, Ed, V>(
                saver = StringDelegate(key, expireDurationInSecond),
                type = object : TypeToken<V>() {},
                defaultValue = defaultValue,
            ) as AbsSpSaver.Delegate<SpSaver, V & Any>
        }
    }
}
