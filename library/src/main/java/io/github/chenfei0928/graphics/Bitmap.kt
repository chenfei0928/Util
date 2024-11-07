package io.github.chenfei0928.graphics

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.IntRange
import io.github.chenfei0928.util.Log
import java.io.File
import java.io.IOException
import java.util.Locale

private const val TAG = "KW_Bitmap"

/**
 * Created by MrFeng on 2018/5/15.
 */
@JvmOverloads
fun Bitmap.save(
    outputFile: File,
    @IntRange(from = 0, to = 100)
    quality: Int = 100
): Boolean {
    return try {
        outputFile.outputStream().use {
            this.compress(getCompressFormat(outputFile.extension), quality, it)
            it.flush()
            true
        }
    } catch (e: IOException) {
        Log.e(TAG, "saveBitmapToFile: ", e)
        false
    }
}

private fun getCompressFormat(
    extName: String,
    default: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
): Bitmap.CompressFormat {
    return when (extName.lowercase(Locale.getDefault())) {
        "jpg", "jpe", "jpeg" ->
            Bitmap.CompressFormat.JPEG  // JPEG
        "jpf", "jpx", "jp2", "j2c", "j2k", "jpc" ->
            Bitmap.CompressFormat.JPEG  // JPEG 2000
        "png", "pns" ->
            Bitmap.CompressFormat.PNG   // PNG
        "webp" ->  // WEBP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSLESS
            } else {
                Bitmap.CompressFormat.WEBP
            }
        else ->
            default
    }
}
