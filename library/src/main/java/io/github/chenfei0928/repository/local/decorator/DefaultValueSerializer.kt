package io.github.chenfei0928.repository.local.decorator

import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.InputStream
import java.io.OutputStream

/**
 * 修改默认值的序列化
 *
 * @author chenfei()
 * @date 2022-07-13 18:54
 */
class DefaultValueSerializer<T : Any>
private constructor(
    private val serializer: LocalSerializer<T>,
    override val defaultValue: T
) : LocalSerializer<T> by serializer, LocalSerializer.IODecorator<T> {

    override fun onOpenInputStream(inputStream: InputStream): InputStream {
        return if (serializer is LocalSerializer.IODecorator) {
            serializer.onOpenInputStream(inputStream)
        } else {
            inputStream
        }
    }

    override fun onOpenOutStream(outputStream: OutputStream): OutputStream {
        return if (serializer is LocalSerializer.IODecorator) {
            serializer.onOpenOutStream(outputStream)
        } else {
            outputStream
        }
    }

    companion object {
        /**
         * 修改默认值的序列化
         */
        fun <T : Any> LocalSerializer<T>.defaultValue(defaultValue: T): LocalSerializer<T> =
            DefaultValueSerializer(this, defaultValue)
    }
}
