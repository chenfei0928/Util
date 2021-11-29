package com.chenfei.storage

import android.content.Context
import android.util.Log
import com.chenfei.util.ExecutorUtil
import java.io.File
import java.io.InputStream
import java.io.OutputStream

abstract class LocalFileStorage<T>(
    serializer: LocalSerializer<T>,
    protected val fileName: String,
    private val cacheDir: Boolean = false,
    private val memoryCacheable: Boolean = true
) {
    private val serializer = NoopIODecorator(serializer)

    protected open fun getFile(context: Context, fileName: String = this.fileName): File {
        val dir = if (cacheDir) {
            context.cacheDir
        } else {
            context.filesDir
        }
        val parentFile = File(dir, "localFileStorage")
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        return File(parentFile, fileName)
    }

    private fun <T> runFileWithLock(context: Context, block: (File) -> T): T {
        return getFile(context, this.fileName + LOCK_FILE_SUFFIX).let {
            ShareFileLockHelper.getFileLock(it)
        }.use {
            getFile(context, this.fileName).let(block)
        }
    }

    /**
     * 将数据序列化到本地文件
     */
    private fun saveToLocalFile(context: Context, data: T): Unit =
        runFileWithLock(context) { file ->
            if (file.exists()) {
                file.delete()
            }
            try {
                file.createNewFile()
                file.outputStream()
                    .let { serializer.onOpenOutStream(it) }
                    .use { serializer.save(it, data) }
            } catch (e: Exception) {
                Log.e(TAG, "loadFromLocalFile: $file, $serializer", e)
                file.delete()
            }
        }

    /**
     * 从本地文件反序列化
     */
    private fun loadFromLocalFile(context: Context): T? = runFileWithLock(context) { file ->
        // 文件不存在，直接返回空
        if (!file.exists()) {
            null
        } else try {
            file.inputStream()
                .let { serializer.onOpenInputStream(it) }
                .use { serializer.load(it) }
        } catch (e: Exception) {
            Log.e(TAG, "loadFromLocalFile: $file, $serializer", e)
            file.delete()
            null
        }
    }

    private fun deleteFile(context: Context) = runFileWithLock(context) { file ->
        file.delete()
    }

    private fun saveToLocalFileOrDelete(context: Context, value: T?) {
        if (value == null) {
            deleteFile(context)
        } else {
            saveToLocalFile(context, value)
        }
    }

    //<editor-fold defaultstate="collapsed" desc="带缓存的快速访问">
    @Volatile
    private var cachedValue: T? = null

    protected var Context.localStorageValue: T?
        @Synchronized
        get() = if (memoryCacheable) {
            cachedValue ?: loadFromLocalFile(this).also { value ->
                cachedValue = value
            }
        } else {
            loadFromLocalFile(this)
        }
        @Synchronized
        set(value) = write(value, false)

    protected fun Context.write(value: T?, writeNow: Boolean = true) {
        // 读取时会拷贝，此处可以不进行拷贝
        if (memoryCacheable) {
            cachedValue = value
        }
        if (memoryCacheable && !writeNow) {
            // 以提交代替同步写入
            ExecutorUtil.postToBg {
                saveToLocalFileOrDelete(this, value)
            }
        } else {
            saveToLocalFileOrDelete(this, value)
        }
    }
    //</editor-fold>

    /**
     * 当子类返回的数据后会对实例进行修改，可能会污染缓存时，
     * 使用实例自身的clone或copy方法，或使用该方法获得一个新的实例后在返回
     */
    protected fun T.serializerCopy(): T {
        return serializer.copy(this)
    }

    companion object {
        protected const val TAG = "KW_LocalJsonStorage"
        private const val LOCK_FILE_SUFFIX = "_lock"
    }

    private class NoopIODecorator<T>(
        serializer: LocalSerializer<T>
    ) : LocalSerializer.BaseIODecorator<T>(serializer) {
        override fun onOpenInputStream1(inputStream: InputStream): InputStream {
            return inputStream
        }

        override fun onOpenOutStream1(outputStream: OutputStream): OutputStream {
            return outputStream
        }
    }
}
