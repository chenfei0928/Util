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
    serializer: LocalSerializer<T>
) : LocalSerializer.BaseIODecorator<T>(serializer) {

    override fun wrapInputStream(inputStream: InputStream): InputStream {
        return GZIPInputStream(inputStream)
    }

    override fun wrapOutputStream(outputStream: OutputStream): OutputStream {
        return GZIPOutputStream(outputStream)
    }

    companion object {
        /**
         * 对数据进行GZip压缩
         */
        fun <T : Any> LocalSerializer<T>.gzip(): LocalSerializer<T> =
            if (this is GZipSerializer) this else GZipSerializer(this)
    }
}
