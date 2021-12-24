package io.github.chenfei0928.storage

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * 用于 [ByteArray] 的序列化和反序列化
 */
class ByteArraySerializer : LocalSerializer<ByteArray> {

    @Throws(IOException::class)
    override fun save(outputStream: OutputStream, obj: ByteArray) {
        outputStream.write(obj)
        outputStream.flush()
    }

    @Throws(IOException::class)
    override fun load(inputStream: InputStream): ByteArray? {
        return inputStream.readBytes()
    }

    override fun copy(obj: ByteArray): ByteArray {
        return obj.copyOf()
    }
}
