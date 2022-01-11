package io.github.chenfei0928.util.gson

import android.os.Build
import android.util.SparseArray
import android.util.SparseLongArray
import com.google.gson.*
import io.github.chenfei0928.reflect.typeOf
import java.io.Reader

inline fun <reified T> Gson.fromJson(json: Reader): T? = fromJson(json, typeOf<T>())

inline fun <reified T> Gson.fromJson(json: String): T? = fromJson(json, typeOf<T>())

inline fun <reified T> GsonBuilder.registerTypeAdapter(typeAdapter: JsonSerializer<T>): GsonBuilder =
    registerTypeAdapter(typeOf<T>(), typeAdapter)

inline fun <reified T> GsonBuilder.registerTypeAdapter(typeAdapter: JsonDeserializer<T>): GsonBuilder =
    registerTypeAdapter(typeOf<T>(), typeAdapter)

inline fun <reified T> GsonBuilder.registerTypeAdapter(typeAdapter: InstanceCreator<T>): GsonBuilder =
    registerTypeAdapter(typeOf<T>(), typeAdapter)

inline fun <reified T> GsonBuilder.registerTypeAdapter(typeAdapter: TypeAdapter<T>): GsonBuilder =
    registerTypeAdapter(typeOf<T>(), typeAdapter)

inline fun <reified R> JsonDeserializationContext.deserialize(json: JsonElement): R =
    deserialize(json, typeOf<R>())

internal val gson: Gson = GsonBuilder()
    .registerTypeAdapter(SparseArray::class.java, BitSetTypeAdapter)
    .registerTypeAdapter(SparseArray::class.java, SparseArrayJsonSerializer)
    .apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            registerTypeAdapter(SparseLongArray::class.java, SparseLongArraySerializer)
        }
    }
    .create()