package io.github.chenfei0928.repository.local

import android.content.Context
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.concurrent.updateAndGetCompat
import io.github.chenfei0928.io.ShareFileLockHelper
import io.github.chenfei0928.io.UncloseableOutputStream
import io.github.chenfei0928.util.Log
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicReference

abstract class LocalFileStorage<T : Any>(
    private val serializer: LocalSerializer<T>,
    private val fileName: String,
    private val cacheDir: Boolean = false,
    private val memoryCacheable: Boolean = true
) {

    private fun getFile(context: Context, fileName: String): File {
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

    //<editor-fold defaultstate="collapsed" desc="通过文件锁读写文件的实现">
    @Synchronized
    private fun <T> runFileWithLock(context: Context, block: (File) -> T): T {
        return getFile(context, this.fileName + LOCK_FILE_SUFFIX).let {
            ShareFileLockHelper.getFileLock(it)
        }.use {
            getFile(context, this.fileName).let(block)
        }
    }

    /**
     * 从本地文件反序列化
     */
    @Suppress("TooGenericExceptionCaught")
    private fun loadFromLocalFile(context: Context): T = runFileWithLock(context) { file ->
        // 文件不存在，直接返回空
        if (!file.exists()) {
            serializer.defaultValue
        } else try {
            file.inputStream()
                .use { serializer.read(it) }
        } catch (e: Exception) {
            Log.e(TAG, "loadFromLocalFile: $file, $serializer", e)
            file.delete()
            serializer.defaultValue
        }
    }

    /**
     * 将数据序列化到本地文件
     */
    @Suppress("TooGenericExceptionCaught")
    private fun saveToLocalFileOrDelete(context: Context, value: T?): Unit =
        runFileWithLock(context) { file ->
            if (value == null) {
                file.delete()
                return@runFileWithLock
            }
            // 先将文件写入临时文件，然后将临时文件更名
            val tmpFile = makeTmpFile(file)
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
            try {
                tmpFile.createNewFile()
                FileSyncOutputStream(tmpFile).use {
                    serializer.write(UncloseableOutputStream(it), value)
                    it.flush()
                }
                // 保存成功后，重命名备份文件
                tmpFile.renameTo(file)
            } catch (e: Exception) {
                Log.e(TAG, "saveToLocalFileOrDelete: $tmpFile, $serializer", e)
                tmpFile.delete()
            }
        }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="带缓存的快速访问">
    private val cachedValue: AtomicReference<T> = AtomicReference()

    protected fun getCacheOrLoad(context: Context): T {
        return if (!memoryCacheable) {
            // 不使用内存缓存，每次都从磁盘文件反序列化
            loadFromLocalFile(context)
        } else {
            // 从缓存中读取，缓存中没有值时从磁盘文件反序列化
            cachedValue.get() ?: cachedValue.updateAndGetCompat {
                it ?: loadFromLocalFile(context)
            }
        }
    }

    protected fun write(context: Context, value: T?, writeNow: Boolean = true) {
        // 读取时会拷贝，此处可以不进行拷贝
        if (memoryCacheable) {
            cachedValue.set(value)
        }
        if (memoryCacheable && !writeNow) {
            // 以提交代替同步写入
            ExecutorUtil.postToBg {
                saveToLocalFileOrDelete(context, value)
            }
        } else {
            saveToLocalFileOrDelete(context, value)
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
        private const val TAG = "Ut_LocalJsonStorage"
        private const val LOCK_FILE_SUFFIX = "_lock"

        private fun makeTmpFile(prefsFile: File): File {
            return File(prefsFile.path + ".tmp")
        }
    }

    private class FileSyncOutputStream : FileOutputStream {
        constructor(name: String) : super(name)
        constructor(name: String, append: Boolean) : super(name, append)
        constructor(file: File) : super(file)
        constructor(file: File, append: Boolean) : super(file, append)
        constructor(fdObj: FileDescriptor) : super(fdObj)

        override fun close() {
            fd.sync()
            super.close()
        }
    }
}
