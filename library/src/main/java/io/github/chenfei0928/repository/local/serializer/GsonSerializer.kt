package io.github.chenfei0928.repository.local.serializer

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import io.github.chenfei0928.repository.local.LocalSerializer
import io.github.chenfei0928.repository.local.decorator.GZipSerializer.Companion.gzip
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * 使用[Gson]来对结构体进行序列化和反序列化
 */
class GsonSerializer<T : Any>(
    gson: Gson = io.github.chenfei0928.json.gson.gson,
    typeToken: TypeToken<T>,
) : LocalSerializer<T> {
    private val typeAdapter: TypeAdapter<T> = gson.getAdapter(typeToken) as TypeAdapter<T>

    @Suppress("UNCHECKED_CAST")
    constructor(
        gson: Gson = io.github.chenfei0928.json.gson.gson,
        type: Type
    ) : this(gson = gson, typeToken = TypeToken.get(type) as TypeToken<T>)

    @Suppress("UNCHECKED_CAST")
    override val defaultValue: T by lazy {
        typeToken.rawType.getDeclaredConstructor().newInstance() as T
    }

    @Throws(IOException::class)
    override fun write(outputStream: OutputStream, obj: T) {
        outputStream.bufferedWriter().use {
            typeAdapter.toJson(it, obj)
            it.flush()
        }
    }

    @Throws(IOException::class)
    override fun read(inputStream: InputStream): T {
        return inputStream.bufferedReader().use {
            typeAdapter.fromJson(it)
        }
    }

    override fun copy(obj: T): T {
        return typeAdapter.fromJsonTree(typeAdapter.toJsonTree(obj))
    }

    companion object {
        /**
         * 快速创建序列化工具实例，避免重复键入类型
         */
        inline operator fun <reified T : Any> invoke() =
            GsonSerializer(typeToken = object : TypeToken<T>() {})

        inline fun <reified T : Any> createGzipped() = invoke<T>().gzip()
    }
}
