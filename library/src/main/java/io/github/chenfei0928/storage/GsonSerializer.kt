package io.github.chenfei0928.storage

import android.os.Build
import android.util.SparseArray
import io.github.chenfei0928.util.SparseArrayJsonSerializer
import io.github.chenfei0928.util.SparseLongArraySerializer
import io.github.chenfei0928.util.kotlin.registerTypeAdapter
import io.github.chenfei0928.util.kotlin.typeOf
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * 使用[Gson]来对结构体进行序列化和反序列化
 */
class GsonSerializer<T>(
    private val gson: Gson = Companion.gson,
    private val type: Type
) : LocalSerializer<T> {

    @Throws(IOException::class)
    override fun save(outputStream: OutputStream, obj: T) {
        outputStream.bufferedWriter().use {
            gson.toJson(obj, type, it)
            it.flush()
        }
    }

    @Throws(IOException::class)
    override fun load(inputStream: InputStream): T? {
        return inputStream.bufferedReader().use {
            gson.fromJson(it, type)
        }
    }

    override fun copy(obj: T): T {
        val json = gson.toJson(obj, type)
        return gson.fromJson(json, type)
    }

    companion object {
        private val gson: Gson = GsonBuilder()
                .registerTypeAdapter(SparseArray::class.java, SparseArrayJsonSerializer())
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        registerTypeAdapter(SparseLongArraySerializer())
                    }
                }
                .create()

        /**
         * 快速创建序列化工具实例，避免重复键入类型
         */
        inline fun <reified T> create() =
                GsonSerializer<T>(type = typeOf<T>())

        inline fun <reified T> createGzipped() =
                GsonSerializer<T>(type = typeOf<T>()).gzip()
    }
}
