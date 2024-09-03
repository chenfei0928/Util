package io.github.chenfei0928.repository.storage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import io.github.chenfei0928.content.FileProviderUtil
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
object FileResolver {
    private const val TAG = "KW_FileResolver"

    fun save(
        context: Context,
        uri: Uri,
        contentValues: ContentValues,
        writer: ContentValuesWriter
    ): Uri? {
        val resolver = context.contentResolver

        val existUri = resolver.query(
            uri,
            arrayOf(MediaStore.MediaColumns._ID),
            "${MediaStore.MediaColumns.DISPLAY_NAME} = ?",
            arrayOf(contentValues.getAsString(MediaStore.MediaColumns.DISPLAY_NAME)),
            null
        )?.use {
            if (it.moveToFirst()) {
                uri.buildUpon().appendPath(it.getLong(0).toString()).build()
            } else {
                null
            }
        }
        if (existUri != null) {
            return existUri
        }

        // 标记文件为即将到来
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
        val insertUri = resolver.insert(uri, contentValues)
        Log.i(TAG, "insertUri: $insertUri")

        return if (insertUri == null) {
            null
        } else try {
            val outputStream = resolver.openOutputStream(insertUri)?.buffered()
                ?: throw IOException()
            outputStream.use {
                writer.write(it)
            }
            // 写入完成后，清除文件未完成即将到来的标记
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(insertUri, contentValues, null, null)
            insertUri
        } catch (e: IOException) {
            Log.e(TAG, "save: $insertUri", e)
            // 写入失败时，删除文件
            try {
                resolver.delete(insertUri, null, null)
            } catch (ignore: Exception) {
                // noop
            }
            null
        }
    }

    fun save(
        context: Context,
        file: File,
        writer: ContentValuesWriter
    ): Boolean = if (file.exists()) {
        true
    } else try {
        // 写入数据
        file.outputStream().buffered().use {
            writer.write(it)
        }
        // 通知媒体库更新一个文件
        FileProviderUtil.createUriFromFile(context, file).let { uri ->
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

interface ContentValuesWriter {
    fun parseArg(host: Fragment, arg: Bundle?): Boolean {
        return true
    }

    @Throws(IOException::class)
    fun write(outputStream: OutputStream)
}
