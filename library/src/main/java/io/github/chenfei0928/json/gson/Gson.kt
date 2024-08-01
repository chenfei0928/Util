package io.github.chenfei0928.json.gson

import android.os.Build
import android.util.SparseArray
import android.util.SparseLongArray
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapter
import io.github.chenfei0928.reflect.typeOf
import java.io.Reader
import java.util.BitSet

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
    .registerTypeAdapter(BitSet::class.java, BitSetTypeAdapter)
    .registerTypeAdapter(SparseArray::class.java, SparseArrayJsonSerializer)
    .apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            registerTypeAdapter(SparseLongArray::class.java, SparseLongArraySerializer)
        }
    }
    .create()
