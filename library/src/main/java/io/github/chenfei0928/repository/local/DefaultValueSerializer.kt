/**
 * 修改默认值的序列化
 *
 * @author chenfei()
 * @date 2022-07-13 18:54
 */
package io.github.chenfei0928.repository.local

import java.io.InputStream
import java.io.OutputStream

/**
 * 修改默认值的序列化
 *
 * @author chenfei()
 * @date 2022-07-13 18:54
 */
internal class DefaultValueSerializer<T>(
    private val serializer: LocalSerializer<T?>,
    override val defaultValue: T
) : LocalSerializer<T>, LocalSerializer.IODecorator {

    override fun write(outputStream: OutputStream, obj: T & Any) {
        serializer.write(outputStream, obj)
    }

    override fun read(inputStream: InputStream): T {
        return serializer.read(inputStream) ?: defaultValue
    }

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
}

/**
 * 修改默认值的序列化
 *
 * @author chenfei()
 * @date 2022-07-13 18:54
 */
fun <T> LocalSerializer<T?>.defaultValue(defaultValue: T): LocalSerializer<T> =
    DefaultValueSerializer(this, defaultValue)
