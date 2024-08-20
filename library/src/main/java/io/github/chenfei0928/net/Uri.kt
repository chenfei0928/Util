package io.github.chenfei0928.net

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.IOException

/**
 * 用于 ContentResolver 的Uri工具
 * Created by MrFeng on 2016/9/19.
 */
fun Context.isExists(uri: Uri?): Boolean {
    return if (uri == null) {
        return false
    } else if (!uri.isLocalUri()) {
        return false
    } else try {
        this.contentResolver.openFileDescriptor(uri, "r").use { }
        true
    } catch (ignore: IOException) {
        false
    }
}

private fun Uri.isLocalUri(): Boolean {
    return ContentResolver.SCHEME_FILE == this.scheme
            || ContentResolver.SCHEME_CONTENT == this.scheme
            || ContentResolver.SCHEME_ANDROID_RESOURCE == this.scheme
}

/**
 * @author chenfei()
 * @date 2022-10-09 17:58
 */
fun Uri.getLength(context: Context): Long {
    if (this@getLength.scheme == ContentResolver.SCHEME_FILE) {
        this@getLength.path?.let {
            try {
                return File(it).length()
            } catch (ignore: Exception) {
                // noop
            }
        }
    }
    context.contentResolver.query(
        this@getLength, arrayOf(OpenableColumns.SIZE, "_data"), null, null, null
    )?.use { cursor ->
        if (!cursor.moveToFirst()) {
            return@use
        }
        // 尝试读取数据库中文件长度
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (!cursor.isNull(sizeIndex)) {
            return cursor.getLong(sizeIndex)
        }
        // 尝试直接读取文件路径来获取文件长度
        val filePathIndex = cursor.getColumnIndex("_data")
        if (filePathIndex >= 0 && !cursor.isNull(filePathIndex)) {
            try {
                return File(cursor.getString(filePathIndex)).length()
            } catch (ignore: Exception) {
                // noop
            }
        }
    }
    try {
        context.contentResolver.openFileDescriptor(
            this@getLength, "r"
        )?.use {
            return it.statSize
        }
    } catch (ignore: Exception) {
        // noop
    }
    try {
        context.contentResolver.openInputStream(this@getLength)?.use {
            return it.available().toLong()
        }
    } catch (ignore: Exception) {
        // noop
    }
    return -1L
}
