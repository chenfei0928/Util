/**
 * 对数据进行GZip压缩
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-08-19 16:48
 */
package io.github.chenfei0928.repository.local

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
class GZipSerializer<T>
private constructor(
    serializer: LocalSerializer<T>
) : LocalSerializer.BaseIODecorator<T>(serializer) {

    override fun onOpenInputStream1(inputStream: InputStream): InputStream {
        return GZIPInputStream(inputStream)
    }

    override fun onOpenOutStream1(outputStream: OutputStream): OutputStream {
        return GZIPOutputStream(outputStream)
    }

    companion object {
        /**
         * 对数据进行GZip压缩
         */
        fun <T> LocalSerializer<T>.gzip(): LocalSerializer<T> {
            return if (this is GZipSerializer) {
                this
            } else {
                GZipSerializer(this)
            }
        }
    }
}
