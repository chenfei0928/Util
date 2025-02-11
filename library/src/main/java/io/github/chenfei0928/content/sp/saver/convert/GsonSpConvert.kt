package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate
import java.lang.reflect.Type

class GsonSpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        T>
constructor(
    saver: AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, String?>,
    private val gson: Gson = io.github.chenfei0928.json.gson.gson,
    private val type: TypeToken<T>,
) : SpConvert<SpSaver, Sp, Ed, String?, T?>(
    saver, PreferenceType.NoSupportPreferenceDataStore
) {

    @Suppress("UNCHECKED_CAST")
    constructor(
        key: String? = null,
        gson: Gson = io.github.chenfei0928.json.gson.gson,
        type: Type,
    ) : this(StringDelegate(key), gson, TypeToken.get(type) as TypeToken<T>)

    override fun onRead(value: String): T & Any = gson.fromJson<T & Any>(value, type)
    override fun onSave(value: T & Any): String = gson.toJson(value)

    companion object {
        inline operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified T> invoke(
            key: String? = null
        ) = GsonSpConvert<SpSaver, Sp, Ed, T>(
            saver = StringDelegate(key), type = object : TypeToken<T>() {}
        )
    }
}
