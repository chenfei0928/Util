package io.github.chenfei0928.io

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream
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
    } catch (e: IOException) {
        false
    }
}

private fun Uri.isLocalUri(): Boolean {
    return (ContentResolver.SCHEME_FILE == this.scheme
            || ContentResolver.SCHEME_CONTENT == this.scheme
            || ContentResolver.SCHEME_ANDROID_RESOURCE == this.scheme)
}

fun copyTo(context: Context, uri: Uri?, dest: File): Boolean {
    return if (uri == null) {
        return false
    } else if (!uri.isLocalUri()) {
        return false
    } else try {
        ParcelFileDescriptor.AutoCloseInputStream(
                context.contentResolver.openFileDescriptor(uri, "r")
        ).use {
            FileOutputStream(dest).use { os ->
                val b = ByteArray(DEFAULT_BUFFER_SIZE)
                var i = it.read(b)
                while (i != -1) {
                    os.write(b, 0, i)
                    i = it.read(b)
                }
                os.flush()
            }
        }
        true
    } catch (e: IOException) {
        false
    }
}
