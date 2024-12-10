package io.github.chenfei0928.content.sp.saver.convert

import com.google.gson.Gson
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate
import io.github.chenfei0928.reflect.jTypeOf
import java.lang.reflect.Type

class GsonSpConvertSaver<T>(
    saver: AbsSpSaver.AbsSpDelegate0<String?>,
    private val gson: Gson = io.github.chenfei0928.json.gson.gson,
    private val type: Type,
) : SpConvertSaver<String?, T?>(saver) {
    @Volatile
    private var cacheValue: Any? = this

    constructor(
        key: String,
        gson: Gson = io.github.chenfei0928.json.gson.gson,
        type: Type
    ) : this(StringDelegate(key), gson, type)

    override fun onRead(value: String?): T? {
        if (cacheValue == this) {
            synchronized(this) {
                if (cacheValue == this) {
                    cacheValue = gson.fromJson(value, type)
                }
            }
        }
        return cacheValue as T?
    }

    override fun onSave(value: T?): String? {
        cacheValue = value
        return gson.toJson(value)
    }

    companion object {
        inline operator fun <reified T> invoke(
            key: String? = null
        ) = GsonSpConvertSaver<T>(StringDelegate(key), type = jTypeOf<T>())
    }
}
