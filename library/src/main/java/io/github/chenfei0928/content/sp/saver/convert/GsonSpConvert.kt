package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
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
        T>
constructor(
    saver: AbsSpSaver.Delegate<SpSaver, String?>,
    private val gson: Gson = io.github.chenfei0928.json.gson.gson,
    private val type: TypeToken<T>,
) : BaseSpConvert<SpSaver, Sp, Ed, String?, T?>(
    saver, PreferenceType.NoSupportPreferenceDataStore
), AbsSpSaver.DefaultValue<T?> {

    override fun onRead(value: String): T & Any = gson.fromJson<T & Any>(value, type)
    override fun onSave(value: T & Any): String = gson.toJson(value)

    class ValueDefaultValue<SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
            Sp : SharedPreferences,
            Ed : SharedPreferences.Editor,
            T>
    constructor(
        saver: AbsSpSaver.Delegate<SpSaver, String?>,
        gson: Gson = io.github.chenfei0928.json.gson.gson,
        type: TypeToken<T>,
        override val defaultValue: T? = null,
    ) : GsonSpConvert<SpSaver, Sp, Ed, T>(saver, gson, type)

    companion object {
        inline operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified T> invoke(
            key: String? = null,
            expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, T?> = ValueDefaultValue<SpSaver, Sp, Ed, T>(
            saver = StringDelegate(key, expireDurationInSecond),
            type = object : TypeToken<T>() {}
        )

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified T> nonnullByBlock(
            key: String? = null,
            expireDurationInSecond: Int = MMKV.ExpireNever,
            noinline defaultValue: () -> T & Any
        ): AbsSpSaver.Delegate<SpSaver, T & Any> {
            @Suppress("UNCHECKED_CAST")
            return object : GsonSpConvert<SpSaver, Sp, Ed, T>(
                saver = StringDelegate(key, expireDurationInSecond),
                type = object : TypeToken<T>() {}
            ), AbsSpSaver.DefaultValue<T & Any> {
                override val defaultValue: T & Any by lazy(defaultValue)
            } as AbsSpSaver.Delegate<SpSaver, T & Any>
        }

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified T> nonnull(
            key: String? = null,
            expireDurationInSecond: Int = MMKV.ExpireNever,
            defaultValue: T & Any
        ): AbsSpSaver.Delegate<SpSaver, T & Any> {
            @Suppress("UNCHECKED_CAST")
            return ValueDefaultValue<SpSaver, Sp, Ed, T>(
                saver = StringDelegate(key, expireDurationInSecond),
                type = object : TypeToken<T>() {},
                defaultValue = defaultValue,
            ) as AbsSpSaver.Delegate<SpSaver, T & Any>
        }
    }
}
