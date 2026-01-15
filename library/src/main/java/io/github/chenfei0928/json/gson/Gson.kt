package io.github.chenfei0928.json.gson

import android.util.SparseArray
import android.util.SparseLongArray
import androidx.annotation.ReturnThis
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapter
import io.github.chenfei0928.reflect.jTypeOf
import java.io.Reader
import java.util.BitSet

inline fun <reified T> Gson.fromJson(json: Reader): T? = fromJson(json, jTypeOf<T>())

inline fun <reified T> Gson.fromJson(json: String): T? = fromJson(json, jTypeOf<T>())

@ReturnThis
inline fun <reified T> GsonBuilder.registerTypeAdapter(typeAdapter: JsonSerializer<T>): GsonBuilder =
    registerTypeAdapter(jTypeOf<T>(), typeAdapter)

@ReturnThis
inline fun <reified T> GsonBuilder.registerTypeAdapter(typeAdapter: JsonDeserializer<T>): GsonBuilder =
    registerTypeAdapter(jTypeOf<T>(), typeAdapter)

@ReturnThis
inline fun <reified T> GsonBuilder.registerTypeAdapter(typeAdapter: InstanceCreator<T>): GsonBuilder =
    registerTypeAdapter(jTypeOf<T>(), typeAdapter)

@ReturnThis
inline fun <reified T> GsonBuilder.registerTypeAdapter(typeAdapter: TypeAdapter<T>): GsonBuilder =
    registerTypeAdapter(jTypeOf<T>(), typeAdapter)

inline fun <reified R> JsonDeserializationContext.deserialize(json: JsonElement): R =
    deserialize(json, jTypeOf<R>())

internal val gson: Gson = GsonBuilder()
    .registerTypeAdapter(BitSet::class.java, BitSetTypeAdapter)
    .registerTypeAdapter(SparseArray::class.java, SparseArrayJsonSerializer)
    .registerTypeAdapter(SparseLongArray::class.java, SparseLongArraySerializer)
    .create()
