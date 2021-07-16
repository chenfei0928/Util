package com.chenfei.module

import java.io.*

/**
 * 对JDK的 [Serializable] 实现序列化和反序列化
 */
class SerializableSerializer<T : Serializable> : LocalSerializer<T> {

    @Throws(IOException::class)
    override fun save(outputStream: OutputStream, obj: T) {
        ObjectOutputStream(outputStream).use {
            it.writeObject(obj)
            it.flush()
        }
    }

    @Throws(IOException::class)
    override fun load(inputStream: InputStream): T? {
        return ObjectInputStream(inputStream).use {
            it.readObject() as T?
        }
    }
}
