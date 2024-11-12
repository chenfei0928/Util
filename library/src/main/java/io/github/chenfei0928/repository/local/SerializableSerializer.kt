package io.github.chenfei0928.repository.local

import androidx.annotation.Discouraged
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.io.Serializable

/**
 * 对JDK的 [Serializable] 实现序列化和反序列化
 */
@Discouraged("don't use Serializable interface")
class SerializableSerializer<T : Serializable>
    : LocalSerializer.IODecorator<T?> {

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
