package io.github.chenfei0928.repository.local

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * 使用[Gson]来对结构体进行序列化和反序列化
 */
class GsonSerializer<T>(
    gson: Gson = io.github.chenfei0928.util.gson.gson,
    typeToken: TypeToken<T>
) : LocalSerializer<T> {
    private val typeAdapter: TypeAdapter<T> = gson.getAdapter(typeToken) as TypeAdapter<T>

    constructor(
        gson: Gson = io.github.chenfei0928.util.gson.gson,
        type: Type
    ) : this(gson = gson, typeToken = TypeToken.get(type) as TypeToken<T>)

    @Throws(IOException::class)
    override fun write(outputStream: OutputStream, obj: T) {
        outputStream.bufferedWriter().use {
            typeAdapter.toJson(it, obj)
            it.flush()
        }
    }

    @Throws(IOException::class)
    override fun read(inputStream: InputStream): T? {
        return inputStream.bufferedReader().use {
            typeAdapter.fromJson(it)
        }
    }

    override fun copy(obj: T): T {
        val json = typeAdapter.toJson(obj)
        return typeAdapter.fromJson(json)
    }

    companion object {
        /**
         * 快速创建序列化工具实例，避免重复键入类型
         */
        inline fun <reified T> create() = GsonSerializer(typeToken = object : TypeToken<T>() {})

        inline fun <reified T> createGzipped() =
            GsonSerializer(typeToken = object : TypeToken<T>() {}).gzip()
    }
}
