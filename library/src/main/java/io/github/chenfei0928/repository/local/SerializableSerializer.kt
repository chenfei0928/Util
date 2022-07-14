package io.github.chenfei0928.repository.local

import java.io.*

/**
 * 对JDK的 [Serializable] 实现序列化和反序列化
 */
class SerializableSerializer<T : Serializable>
    : LocalSerializer<T?>, LocalSerializer.IODecorator {

    override val defaultValue: T? = null

    @Throws(IOException::class)
    override fun write(outputStream: OutputStream, obj: T) {
        (outputStream as ObjectOutputStream).run {
            writeObject(obj)
            flush()
        }
    }

    @Throws(IOException::class)
    override fun read(inputStream: InputStream): T {
        return (inputStream as ObjectInputStream).run {
            readObject() as T
        }
    }

    override fun onOpenInputStream(inputStream: InputStream): InputStream {
        return ObjectInputStream(inputStream)
    }

    override fun onOpenOutStream(outputStream: OutputStream): OutputStream {
        return ObjectOutputStream(outputStream)
    }
}
