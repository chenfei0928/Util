package com.chenfei.module

import android.os.Build
import android.util.SparseArray
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.chenfei.util.GsonBoolean
import com.chenfei.util.SparseArrayJsonSerializer
import com.chenfei.util.SparseLongArraySerializer
import com.chenfei.util.kotlin.registerTypeAdapter
import com.chenfei.util.kotlin.typeOf
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * 使用[Gson]来对结构体进行序列化和反序列化
 */
class GsonSerializer<T>(
    private val gson: Gson = GsonSerializer.gson,
    private val type: Type
) : LocalSerializer<T> {

    @Throws(IOException::class)
    override fun save(outputStream: OutputStream, obj: T) {
        outputStream.bufferedWriter().use {
            it.write(gson.toJson(obj))
            it.flush()
        }
    }

    @Throws(IOException::class)
    override fun load(inputStream: InputStream): T? {
        return inputStream.bufferedReader().use {
            gson.fromJson(it, type)
        }
    }

    companion object {
        private val gson: Gson = GsonBuilder()
            .registerTypeAdapter(SparseArray::class.java, SparseArrayJsonSerializer())
            .registerTypeAdapterFactory(GsonBoolean.factory)
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
    }
}
