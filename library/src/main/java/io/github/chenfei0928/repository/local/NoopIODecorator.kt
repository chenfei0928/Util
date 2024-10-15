package io.github.chenfei0928.repository.local

import io.github.chenfei0928.repository.local.LocalSerializer.BaseIODecorator
import java.io.InputStream
import java.io.OutputStream

/**
 * 复用父类对IO流的判断封装处理，并不对io流进行任何处理
 *
 * @author chenf()
 * @date 2024-08-20 15:29
 */
internal class NoopIODecorator<T>
private constructor(
    serializer: LocalSerializer<T>
) : BaseIODecorator<T>(serializer) {
    override fun wrapOutputStream(outputStream: OutputStream): OutputStream {
        return outputStream
    }

    override fun wrapInputStream(inputStream: InputStream): InputStream {
        return inputStream
    }

    companion object {
        fun <T> wrap(serializer: LocalSerializer<T>): LocalSerializer.IODecorator<T> =
            if (serializer is LocalSerializer.IODecorator) serializer
            else NoopIODecorator(serializer)
    }
}
