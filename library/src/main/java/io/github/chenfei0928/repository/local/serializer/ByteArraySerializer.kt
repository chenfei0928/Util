package io.github.chenfei0928.repository.local.serializer

import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * 用于 [ByteArray] 的序列化和反序列化
 */
object ByteArraySerializer : LocalSerializer<ByteArray> {
    override val defaultValue: ByteArray = byteArrayOf()

    @Throws(IOException::class)
    override fun write(outputStream: OutputStream, obj: ByteArray) {
        outputStream.write(obj)
        outputStream.flush()
    }

    @Throws(IOException::class)
    override fun read(inputStream: InputStream): ByteArray {
        return inputStream.readBytes()
    }

    override fun copy(obj: ByteArray): ByteArray {
        return obj.copyOf()
    }

    override fun toString(): String {
        return "ByteArraySerializer"
    }
}
