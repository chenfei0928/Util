package com.chenfei.retrofitUtil

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.chenfei.util.FileUtil
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException

/**
 * Created by MrFeng on 2017/5/8.
 */
internal class UriRequestBody(
    private val context: Context, private val uri: Uri
) : RequestBody() {

    override fun contentType(): MediaType? {
        val documentUriMediaType =
            DocumentFile.fromSingleUri(context, uri)?.type?.toMediaTypeOrNull()
        return documentUriMediaType ?: FileUtil
            .getFileExtensionFromUrl(uri.toString())
            .toMediaTypeOrNull()
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return context.contentResolver
            .openInputStream(uri)
            ?.use {
                it
                    .available()
                    .toLong()
            } ?: super.contentLength()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        context.contentResolver
            .openInputStream(uri)
            ?.source()
            ?.use { source ->
                sink.writeAll(source)
            }
    }
}
