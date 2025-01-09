package io.github.chenfei0928.content.sp.saver.convert

import com.google.gson.Gson
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate
import io.github.chenfei0928.reflect.jTypeOf
import java.lang.reflect.Type

class GsonSpConvertSaver<T>(
    saver: AbsSpSaver.AbsSpDelegate<String?>,
    private val gson: Gson = io.github.chenfei0928.json.gson.gson,
    private val type: Type,
) : SpConvertSaver<String?, T?>(saver, PreferenceType.NoSupportPreferenceDataStore) {
    @Volatile
    private var cacheValue: Pair<String, T>? = null

    constructor(
        key: String,
        gson: Gson = io.github.chenfei0928.json.gson.gson,
        type: Type
    ) : this(StringDelegate(key), gson, type)

    override fun onRead(value: String?): T? {
        val cacheValue = cacheValue
        return if (cacheValue?.first == value) {
            cacheValue?.second
        } else value?.let {
            val t = gson.fromJson<T>(value, type)
            this.cacheValue = it to t
            t
        }
    }

    override fun onSave(value: T?): String? = value?.let {
        val json = gson.toJson(value)
        cacheValue = json to value
        json
    }

    companion object {
        inline operator fun <reified T> invoke(
            key: String? = null
        ) = GsonSpConvertSaver<T>(StringDelegate(key), type = jTypeOf<T>())
    }
}
