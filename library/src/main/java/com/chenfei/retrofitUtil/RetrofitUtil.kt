package com.chenfei.retrofitUtil

import android.content.Context
import android.net.Uri
import android.os.Build
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URLConnection
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * User: ChenFei(chenfei0928@gmail.com)
 * Date: 2016-06-04
 * Time: 18:46
 */
object RetrofitUtil {
    @JvmField
    val jsonMediaType: MediaType = "application/json".toMediaType()
    val octetStreamType: MediaType = "application/octet-stream".toMediaType()

    @JvmField
    val utf8Charset: Charset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        StandardCharsets.UTF_8
    } else {
        Charset.forName("UTF-8")
    }

    @JvmStatic
    fun wrap(content: String?): RequestBody? =
        if (content.isNullOrEmpty()) null else content.toRequestBody(null)

    fun wrap(key: String, file: File): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            key, file.name, file.asRequestBody(
                URLConnection
                    .getFileNameMap()
                    .getContentTypeFor(file.name)
                    .toMediaTypeOrNull() ?: octetStreamType
            )
        )
    }

    @JvmStatic
    fun wrap(context: Context, key: String, uri: Uri): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            key, uri.pathSegments[uri.pathSegments.size - 1], UriRequestBody(context, uri)
        )
    }

    @JvmStatic
    fun <T> joinToString(values: List<T>?, getId: IdGetter<T>): String? {
        return if (values == null || values.isEmpty()) {
            null
        } else {
            values.joinToString(separator = ",", transform = {
                getId
                    .getId(it)
                    .toString()
            })
        }
    }

    interface IdGetter<T> {
        fun getId(t: T): Int
    }
}
