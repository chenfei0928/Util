package io.github.chenfei0928.net.retrofit

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import io.github.chenfei0928.net.getLength
import io.github.chenfei0928.io.FileUtil
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException

/**
 * 使用Intent返回的Uri读取文件内容
 *
 * Created by MrFeng on 2017/5/8.
 */
class UriRequestBody(
    private val context: Context,
    private val uri: Uri
) : RequestBody() {

    override fun contentType(): MediaType? {
        return DocumentFile.fromSingleUri(context, uri)?.type?.toMediaTypeOrNull()
            ?: MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(FileUtil.getFileExtensionFromUrl(uri.toString()))
                ?.toMediaTypeOrNull()
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return uri.getLength(context)
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
