package io.github.chenfei0928.repository.local

import android.content.Context
import android.os.Build
import android.util.Log
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.io.ShareFileLockHelper
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicReference

abstract class LocalFileStorage<T>(
    serializer: LocalSerializer<T>,
    private val fileName: String,
    private val cacheDir: Boolean = false,
    private val memoryCacheable: Boolean = true
) {
    private val serializer = NoopIODecorator(serializer)

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
    private fun loadFromLocalFile(context: Context): T? = runFileWithLock(context) { file ->
        // 如果备份文件存在，说明上次的保存没有成功，删除未成功保存的文件，读取备份文件
        makeBackupFile(file).let { backupFile ->
            if (backupFile.exists()) {
                file.delete()
                backupFile.renameTo(file)
            }
        }
        // 文件不存在，直接返回空
        if (!file.exists()) {
            null
        } else try {
            file.inputStream()
                .let { serializer.onOpenInputStream(it) }
                .use { serializer.read(it) }
        } catch (e: Exception) {
            Log.e(TAG, "loadFromLocalFile: $file, $serializer", e)
            file.delete()
            null
        }
    }

    /**
     * 将数据序列化到本地文件
     */
    private fun saveToLocalFileOrDelete(context: Context, value: T?): Unit =
        runFileWithLock(context) { file ->
            val backupFile = makeBackupFile(file)
            // 如果备份文件存在，将主文件删除后写入主文件
            // 备份文件在主文件写入成功前不会被删除，即备份文件总是曾经有效的某次写入
            if (backupFile.exists()) {
                file.delete()
            } else if (!file.renameTo(backupFile)) {
                // 备份文件不存在，并将主文件重命名为备份文件时失败，不写入，以防写入失败后数据彻底损坏
                Log.e(TAG, "Couldn't rename file $file to backup file $backupFile")
                return@runFileWithLock
            }
            // 上方已将主文件重命名或删除，主文件已不存在，此时需要写入空数据时，直接删除备份文件返回
            if (value == null) {
                backupFile.delete()
                return@runFileWithLock
            }
            try {
                file.createNewFile()
                file.outputStream()
                    .let { serializer.onOpenOutStream(it) }
                    .use { serializer.write(it, value) }
                // 保存成功后，删除备份文件
                backupFile.delete()
            } catch (e: Exception) {
                Log.e(TAG, "loadFromLocalFile: $file, $serializer", e)
                file.delete()
            }
        }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="带缓存的快速访问">
    private val cachedValue: AtomicReference<T> = AtomicReference()

    protected fun getCacheOrLoad(context: Context): T? {
        return if (!memoryCacheable) {
            // 不使用内存缓存，每次都从磁盘文件反序列化
            loadFromLocalFile(context)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 从缓存中读取，缓存中没有值时从磁盘文件反序列化
            cachedValue.get() ?: cachedValue.updateAndGet {
                loadFromLocalFile(context)
            }
        } else synchronized(this) {
            // 从缓存中读取，缓存中没有值时从磁盘文件反序列化
            cachedValue.get() ?: loadFromLocalFile(context)?.also {
                cachedValue.set(it)
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
        protected const val TAG = "KW_LocalJsonStorage"
        private const val LOCK_FILE_SUFFIX = "_lock"

        private fun makeBackupFile(prefsFile: File): File {
            return File(prefsFile.path + ".bak")
        }
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
