package io.github.chenfei0928.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by MrFeng on 2018/1/31.
 */
class FileUtil {
    companion object {

        @JvmStatic
        fun joinPath(vararg path: String): String {
            return path.joinToString(File.separator)
        }

        @JvmStatic
        fun deleteFileOrDir(path: File?): Boolean {
            if (path == null || !path.exists()) {
                return true
            }
            if (path.isFile) {
                return path.delete()
            }
            path
                .listFiles()
                ?.forEach {
                    deleteFileOrDir(it)
                }
            return path.delete()
        }

        @JvmStatic
        fun getFileOrDirSize(file: File?): Long {
            if (file == null) {
                return 0
            }
            if (!file.exists()) {
                return 0
            }
            if (!file.isDirectory) {
                return file.length()
            }

            var length: Long = 0
            file
                .listFiles()
                ?.forEach {
                    length += getFileOrDirSize(it)
                }
            return length
        }

        @JvmStatic
        fun moveDirToDest(source: File, dest: File): Boolean {
            if (source.isFile) {
                return copyFileToDest(source, dest) && deleteFileOrDir(source)
            }
            if (!dest.exists()) {
                dest.mkdir()
            }
            val files = source.listFiles()
            if (files == null) {
                return false
            } else {
                var finish = true
                for (file in files) {
                    val name = file.name
                    val destChild = File(dest, name)
                    if (!moveDirToDest(file, destChild)) {
                        finish = false
                    }
                }
                return if (finish) {
                    deleteFileOrDir(source)
                    true
                } else {
                    false
                }
            }
        }

        @JvmStatic
        fun copyFileToDest(source: File, dest: File): Boolean {
            if (!dest.exists()) {
                try {
                    dest.createNewFile()
                } catch (e: IOException) {
                }
            }
            return try {
                FileInputStream(source).use { fis ->
                    FileOutputStream(dest).use { fos ->
                        val inputChannel = fis.channel
                        fos.channel.transferFrom(inputChannel, 0, inputChannel.size())
                        fos.flush()
                        return true
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }

        @JvmStatic
        fun copyUriToDestFile(context: Context, source: Uri, dest: File): Boolean {
            if (!dest.exists()) {
                try {
                    dest.createNewFile()
                } catch (e: IOException) {
                }
            }
            return try {
                val fis = context.contentResolver.openInputStream(source) ?: return false
                fis.use {
                    FileOutputStream(dest).use { fos ->
                        fos
                            .sink()
                            .buffer()
                            .writeAll(fis.source())
                        fos.flush()
                        return true
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }

        /**
         * 获取扩展名，实现方式复制于：
         * [android.webkit.MimeTypeMap.getFileExtensionFromUrl]
         */
        @JvmStatic
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
        @JvmOverloads
        fun getExternalStorageDir(type: String, dirName: String = "YikeTalks"): File? {
            val typeDir = Environment.getExternalStoragePublicDirectory(type) ?: return null
            val dir = File(typeDir, dirName)
            // 如路径不存在，创建文件夹路径
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }
    }
}
