package io.github.chenfei0928.repository.storage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import io.github.chenfei0928.content.FileProviderKt
import io.github.chenfei0928.util.Log
import java.io.File
import java.io.IOException
import java.io.OutputStream

/**
 * 保存到系统所提供的 contentResolver 目录
 * [博文](https://mp.weixin.qq.com/s/aiDMyAfAZvaYIHuIMLAlcg)
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-10 16:46
 */
class FileResolver {
    companion object {
        private const val TAG = "KW_FileResolver"

        fun save(
            context: Context, uri: Uri, contentValues: ContentValues, writer: ContentValuesWriter
        ): Boolean {
            // 标记文件为即将到来
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 1)
            val resolver = context.contentResolver
            val insertUri = resolver.insert(uri, contentValues)
            Log.i(TAG, "insertUri: $insertUri")

            if (insertUri == null) {
                return false
            }
            try {
                val outputStream = resolver
                    .openOutputStream(insertUri)
                    ?.buffered() ?: return false
                outputStream.use {
                    writer.write(it)
                    // 写入完成后，清除文件未完成即将到来的标记
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(insertUri, contentValues, null, null)
                    return true
                }
            } catch (e: IOException) {
                Log.e(TAG, "save: $insertUri", e)
                // 写入失败时，删除文件
                try {
                    resolver.delete(insertUri, null, null)
                } catch (e: Exception) {
                }
                return false
            }
        }

        fun save(context: Context, file: File, writer: ContentValuesWriter): Boolean {
            return try {
                // 写入数据
                file
                    .outputStream()
                    .buffered()
                    .use {
                        writer.write(it)
                    }
                // 通知媒体库更新一个文件
                FileProviderKt.createUriFromFile(context, file)?.let { uri ->
                    val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    scanIntent.data = uri
                    context.sendBroadcast(scanIntent)
                }
                true
            } catch (e: IOException) {
                Log.e(TAG, "save: $file", e)
                false
            }
        }
    }
}

interface ContentValuesWriter {
    fun parseArg(host: Fragment, arg: Bundle?): Boolean {
        return true
    }

    @Throws(IOException::class)
    fun write(outputStream: OutputStream)
}
