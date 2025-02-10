package io.github.chenfei0928.content.sp.saver.convert

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate
import java.lang.reflect.Type

class GsonSpConvertSaver<SpSaver : AbsSpSaver<SpSaver>, T>(
    saver: AbsSpSaver.AbsSpDelegateImpl<SpSaver, String?>,
    private val gson: Gson = io.github.chenfei0928.json.gson.gson,
    private val type: TypeToken<T>,
) : SpConvertSaver<SpSaver, String?, T?>(saver, PreferenceType.NoSupportPreferenceDataStore) {

    @Suppress("UNCHECKED_CAST")
    constructor(
        key: String? = null,
        gson: Gson = io.github.chenfei0928.json.gson.gson,
        type: Type,
    ) : this(StringDelegate(key), gson, TypeToken.get(type) as TypeToken<T>)

    override fun onRead(value: String): T & Any = gson.fromJson<T & Any>(value, type)
    override fun onSave(value: T & Any): String = gson.toJson(value)

    companion object {
        inline operator fun <SpSaver : AbsSpSaver<SpSaver>, reified T> invoke(
            key: String? = null
        ) = GsonSpConvertSaver<SpSaver, T>(StringDelegate(key), type = object : TypeToken<T>() {})
    }
}
