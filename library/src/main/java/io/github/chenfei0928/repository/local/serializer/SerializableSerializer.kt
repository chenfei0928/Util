package io.github.chenfei0928.repository.local.serializer

import androidx.annotation.Discouraged
import io.github.chenfei0928.repository.local.LocalSerializer
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
class SerializableSerializer<T : Serializable>(
    override val defaultValue: T
) : LocalSerializer<T> {

    @Throws(IOException::class)
    override fun write(outputStream: OutputStream, obj: T) {
        ObjectOutputStream(outputStream).use {
            it.writeObject(obj)
            it.flush()
        }
    }

    @Throws(IOException::class)
    override fun read(inputStream: InputStream): T {
        return ObjectInputStream(inputStream).use {
            @Suppress("UNCHECKED_CAST")
            it.readObject() as T
        }
    }
}
