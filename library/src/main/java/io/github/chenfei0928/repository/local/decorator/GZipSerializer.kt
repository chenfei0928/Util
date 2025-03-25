/**
 * 对数据进行GZip压缩
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-08-19 16:48
 */
package io.github.chenfei0928.repository.local.decorator

import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * 对数据进行GZip压缩
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-08-19 16:48
 */
class GZipSerializer<T : Any>
private constructor(
    private val serializer: LocalSerializer<T>
) : LocalSerializer<T> by serializer {

    override fun read(inputStream: InputStream): T {
        return GZIPInputStream(inputStream).use {
            serializer.read(it)
        }
    }

    override fun write(outputStream: OutputStream, obj: T) {
        GZIPOutputStream(outputStream).use {
            serializer.write(it, obj)
            it.flush()
        }
    }

    override fun toString(): String {
        return "GZipSerializer(serializer=$serializer)"
    }

    companion object {
        /**
         * 对数据进行GZip压缩
         */
        fun <T : Any> LocalSerializer<T>.gzip(): LocalSerializer<T> =
            this as? GZipSerializer ?: GZipSerializer(this)
    }
}
