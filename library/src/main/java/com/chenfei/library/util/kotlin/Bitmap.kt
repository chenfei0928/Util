package com.chenfei.library.util.kotlin

import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.io.FileOutputStream

private const val TAG = "KW_Bitmap"

/**
 * Created by MrFeng on 2018/5/15.
 */
@JvmOverloads
fun Bitmap.save(outputFile: File, quality: Int = 100): Boolean {
    return try {
        FileOutputStream(outputFile).use {
            this.compress(getCompressFormat(outputFile.extension), quality, it)
            it.flush()
            true
        }
    } catch (e: Throwable) {
        Log.e(TAG, "saveBitmapToFile: ", e)
        false
    }
}

private fun getCompressFormat(extName: String, default: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG): Bitmap.CompressFormat {
    return when (extName.toLowerCase()) {
        "jpg", "jpe", "jpeg" ->
            Bitmap.CompressFormat.JPEG  // JPEG
        "jpf", "jpx", "jp2", "j2c", "j2k", "jpc" ->
            Bitmap.CompressFormat.JPEG  // JPEG 2000
        "png", "pns" ->
            Bitmap.CompressFormat.PNG   // PNG
        "webp" ->
            Bitmap.CompressFormat.WEBP  // WEBP
        else ->
            default
    }
}
