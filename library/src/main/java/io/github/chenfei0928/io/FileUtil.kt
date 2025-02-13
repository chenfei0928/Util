package io.github.chenfei0928.io

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.os.ParcelFileDescriptor
import android.util.Log
import android.webkit.MimeTypeMap
import okio.buffer
import okio.sink
import okio.source
import okio.use
import java.io.File
import java.io.IOException

/**
 * @author MrFeng
 * @date 2018/1/31.
 */
object FileUtil {
    private const val TAG = "KW_FileUtil"

    @JvmStatic
    fun joinPath(vararg paths: String): String {
        return paths.joinToString(File.separator)
    }

    /**
     * 迭代删除一个文件或目录
     *
     * @param path 要删除的文件或文件夹
     * @return 删除成功返回true，失败则返回false
     */
    fun deleteFileOrDir(
        path: File?
    ): Boolean = if (path == null || !path.exists()) {
        // 文件不存在，直接返回
        true
    } else if (path.isFile) {
        // 文件路径，删除
        path.delete()
    } else {
        // 非文件路径，迭代子路径，删除
        path.listFiles()?.forEach {
            deleteFileOrDir(it)
        }
        path.delete()
    }

    /**
     * 获取一个文件或目录的大小
     *
     * @param file 要获取文件尺寸的文件或目录
     * @return 文件尺寸，以字节为单位
     */
    fun getFileOrDirSize(
        file: File?
    ): Long = if (file == null || !file.exists()) {
        // 文件不存在，返回0
        0
    } else if (!file.isDirectory) {
        // 路径不是文件夹，返回文件大小
        file.length()
    } else {
        // 迭代子文件，并统计总大小
        file.listFiles()
            ?.sumOf { getFileOrDirSize(it) }
            ?: 0
    }

    @JvmStatic
    fun moveDirToDest(
        source: File, dest: File
    ): Boolean = if (source.isFile) {
        copyFileToDest(source, dest) && deleteFileOrDir(source)
    } else if (!source.isDirectory) {
        false
    } else if (!dest.exists() && !dest.mkdirs()) {
        false
    } else {
        // 不是目录（但上方已判断是否为目录），或发生io错误时会返回null
        // 此处返回null时为发生了io错误，返回false
        val files = source.listFiles()
            ?: return false
        var finish = true
        files.forEach { file ->
            if (!moveDirToDest(file, File(dest, file.name))) {
                finish = false
            }
        }
        if (finish) {
            deleteFileOrDir(source)
            true
        } else {
            false
        }
    }

    @JvmStatic
    @Suppress("NestedBlockDepth")
    fun copyFileToDest(source: File, dest: File): Boolean {
        if (!dest.exists()) {
            dest.parentFile?.mkdirs()
            try {
                dest.createNewFile()
            } catch (_: IOException) {
                // noop
            }
        }
        try {
            source.inputStream().use { fis ->
                dest.outputStream().use { fos ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val fosFd = fos.fd
                        FileUtils.copy(fis.fd, fosFd)
                        fosFd.sync()
                    } else {
                        fis.channel.use { inputChannel ->
                            fos.channel.use { fosChannel ->
                                fosChannel.transferFrom(inputChannel, 0, inputChannel.size())
                                fos.flush()
                            }
                        }
                    }
                    return true
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "copyFileToDest: $source to $dest", e)
            return false
        }
    }

    @JvmStatic
    @Suppress("NestedBlockDepth", "ReturnCount")
    fun copyUriToDestFile(context: Context, source: Uri?, dest: File): Boolean {
        if (source == null) {
            return false
        }
        if (!dest.exists()) {
            dest.parentFile?.mkdirs()
            try {
                dest.createNewFile()
            } catch (_: IOException) {
                // noop
            }
        }
        try {
            context.contentResolver.openFileDescriptor(source, "r")?.use { fis ->
                dest.outputStream().use { fos ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        FileUtils.copy(fis.fileDescriptor, fos.fd)
                        fos.flush()
                    } else {
                        ParcelFileDescriptor.AutoCloseInputStream(fis).use { inputStream ->
                            inputStream.copyTo(fos)
                            fos.flush()
                        }
                    }
                    return true
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "copyUriToDestFile: openFileDescriptor $source to $dest", e)
            dest.delete()
            dest.createNewFile()
        }
        try {
            context.contentResolver.openInputStream(source)?.use { fis ->
                dest.outputStream().use { fos ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        FileUtils.copy(fis, fos)
                    } else {
                        fos.sink().buffer().writeAll(fis.source())
                    }
                    fos.flush()
                    return true
                }
            }
            return false
        } catch (e: IOException) {
            Log.w(TAG, "copyUriToDestFile: openInputStream $source to $dest", e)
            return false
        }
    }

    /**
     * 获取扩展名，实现方式复制于：
     * [android.webkit.MimeTypeMap.getFileExtensionFromUrl]
     */
    @JvmStatic
    @Suppress("ReturnCount")
    fun getFileExtensionFromUrl(url: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension.isNotEmpty()) {
            return extension
        }
        @Suppress("NAME_SHADOWING") var url = url
        if (url.isNotEmpty()) {
            val fragment = url.lastIndexOf('#')
            if (fragment > 0) {
                url = url.substring(0, fragment)
            }

            val query = url.lastIndexOf('?')
            if (query > 0) {
                url = url.substring(0, query)
            }

            val filenamePos = url.lastIndexOf('/')
            val filename = if (0 <= filenamePos) url.substring(filenamePos + 1) else url

            if (filename.isNotEmpty()) {
                val dotPos = filename.lastIndexOf('.')
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1)
                }
            }
        }
        return ""
    }

    /**
     * 获取扩展卡目录，用于保存文件
     *
     * @param type    参见[Environment.getExternalStoragePublicDirectory]的参数说明，为其类的常量之一
     * @param dirName 目标文件夹名，为应用识别名
     */
    @JvmStatic
    fun getExternalStorageDir(type: String, dirName: String): File? {
        val typeDir = Environment.getExternalStoragePublicDirectory(type)
            ?: return null
        val dir = File(typeDir, dirName)
        // 如路径不存在，创建文件夹路径
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}
