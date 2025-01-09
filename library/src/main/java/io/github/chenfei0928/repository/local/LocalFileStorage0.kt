package io.github.chenfei0928.repository.local

import android.content.Context
import android.util.AtomicFile
import android.util.Log
import androidx.core.util.tryWrite
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.concurrent.updateAndGetCompat
import java.io.File
import java.util.concurrent.atomic.AtomicReference

abstract class LocalFileStorage0<T : Any>(
    file: File,
    serializer: LocalSerializer<T>,
    private val memoryCacheable: Boolean = true
) {
    constructor(
        context: Context,
        fileName: String,
        serializer: LocalSerializer<T>,
        cacheDir: Boolean = false,
        memoryCacheable: Boolean = true
    ) : this(
        file = if (cacheDir) {
            File(File(context.cacheDir, "localFileStorage"), fileName)
        } else {
            File(File(context.filesDir, "localFileStorage"), fileName)
        },
        serializer = serializer,
        memoryCacheable = memoryCacheable
    )

    private val serializer = NoopIODecorator.wrap(serializer)
    private val atomicFile: AtomicFile = AtomicFile(file)

    //<editor-fold defaultstate="collapsed" desc="通过文件锁读写文件的实现">
    /**
     * 从本地文件反序列化
     */
    @Synchronized
    @Suppress("TooGenericExceptionCaught")
    private fun loadFromLocalFile(): T {
        return if (!atomicFile.baseFile.exists()) {
            // 文件不存在，直接返回空
            serializer.defaultValue
        } else try {
            atomicFile.openRead()
                .let { serializer.onOpenInputStream(it) }
                .use { serializer.read(it) }
        } catch (e: Exception) {
            Log.e(TAG, "loadFromLocalFile: ${atomicFile.baseFile}, $serializer", e)
            atomicFile.delete()
            serializer.defaultValue
        }
    }

    /**
     * 将数据序列化到本地文件
     */
    @Synchronized
    @Suppress("TooGenericExceptionCaught")
    private fun saveToLocalFileOrDelete(value: T?) {
        if (value == null) {
            atomicFile.delete()
            return
        }
        try {
            atomicFile.tryWrite { write ->
                serializer.onOpenOutStream(write).use {
                    serializer.write(it, value)
                    it.flush()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveToLocalFileOrDelete: ${atomicFile.baseFile}, $serializer", e)
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="带缓存的快速访问">
    private val cachedValue: AtomicReference<T> = AtomicReference()

    protected fun getCacheOrLoad(): T {
        return if (!memoryCacheable) {
            // 不使用内存缓存，每次都从磁盘文件反序列化
            loadFromLocalFile()
        } else {
            // 从缓存中读取，缓存中没有值时从磁盘文件反序列化
            cachedValue.get() ?: cachedValue.updateAndGetCompat {
                loadFromLocalFile()
            }
        }
    }

    protected fun write(value: T?, writeNow: Boolean = true) {
        // 读取时会拷贝，此处可以不进行拷贝
        if (memoryCacheable) {
            cachedValue.set(value)
        }
        if (memoryCacheable && !writeNow) {
            // 以提交代替同步写入
            ExecutorUtil.postToBg {
                saveToLocalFileOrDelete(value)
            }
        } else {
            saveToLocalFileOrDelete(value)
        }
    }
    //</editor-fold>

    /**
     * 当子类返回的数据后会对实例进行修改，可能会污染缓存时，
     * 使用实例自身的clone或copy方法，或使用该方法获得一个新的实例后在返回
     */
    protected fun <Tn : T> Tn.serializerCopy(): T {
        return serializer.copy(this)
    }

    companion object {
        private const val TAG = "KW_LocalJsonStorage"
    }
}
