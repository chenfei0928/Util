package io.github.chenfei0928.storage

import java.io.*

/**
 * 本地文件保存序列化接口
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-05 12:06
 */
interface LocalSerializer<T> {
    @Throws(IOException::class)
    fun save(outputStream: OutputStream, obj: T)

    @Throws(IOException::class)
    fun load(inputStream: InputStream): T?

    fun copy(obj: T): T {
        return ByteArrayOutputStream().use {
            save(it, obj)
            it.toByteArray()
        }.let { ByteArrayInputStream(it) }.use {
            load(it)!!
        }
    }

    /**
     * IO装饰器，用于不对内容进行真正的写入操作，而是对IO流的装饰器。
     *
     * 实现时需要先判断序列化实现是否也是一个装饰器，建议继承[BaseIODecorator]类来实现。
     */
    interface IODecorator {
        @Throws(IOException::class)
        fun onOpenInputStream(inputStream: InputStream): InputStream

        @Throws(IOException::class)
        fun onOpenOutStream(outputStream: OutputStream): OutputStream
    }

    abstract class BaseIODecorator<T>(
            private val serializer: LocalSerializer<T>
    ) : LocalSerializer<T> by serializer, IODecorator {

        override fun onOpenInputStream(inputStream: InputStream): InputStream {
            return if (serializer is IODecorator) {
                serializer.onOpenInputStream(onOpenInputStream1(inputStream))
            } else {
                onOpenInputStream1(inputStream)
            }
        }

        override fun onOpenOutStream(outputStream: OutputStream): OutputStream {
            return if (serializer is IODecorator) {
                serializer.onOpenOutStream(onOpenOutStream1(outputStream))
            } else {
                onOpenOutStream1(outputStream)
            }
        }

        @Throws(IOException::class)
        abstract fun onOpenInputStream1(inputStream: InputStream): InputStream

        @Throws(IOException::class)
        abstract fun onOpenOutStream1(outputStream: OutputStream): OutputStream
    }
}
