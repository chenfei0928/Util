package com.chenfei.module

import android.content.Context
import com.chenfei.util.Log
import java.io.File

abstract class LocalFileModule<T>(
        private val serializer: LocalSerializer<T>,
        protected val fileName: String,
        private val cacheDir: Boolean = false
) {
    protected open fun getFile(context: Context, fileName: String = this.fileName): File {
        val dir = if (cacheDir) {
            context.cacheDir
        } else {
            context.filesDir
        }
        return File(dir, fileName)
    }

    /**
     * 将数据序列化到本地文件
     */
    protected fun saveToLocalFile(context: Context, data: T) {
        val file = getFile(context)
        if (file.exists()) {
            file.delete()
        }
        try {
            file.createNewFile()
            file.outputStream().use {
                serializer.save(it, data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "loadFromLocalFile: $file, $serializer", e)
            file.delete()
        }
    }

    /**
     * 从本地文件反序列化
     */
    protected fun loadFromLocalFile(context: Context): T? {
        val file = getFile(context)
        // 文件不存在，直接返回空
        if (!file.exists()) {
            return null
        }
        return try {
            file.inputStream().use {
                serializer.load(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "loadFromLocalFile: $file, $serializer", e)
            file.delete()
            null
        }
    }

    protected fun deleteFile(context: Context) {
        getFile(context).delete()
    }

    companion object {
        protected const val TAG = "KW_LocalJsonModule"
    }
}
