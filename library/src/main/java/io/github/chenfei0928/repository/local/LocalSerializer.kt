package io.github.chenfei0928.repository.local

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * 本地文件保存序列化接口
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-05 12:06
 */
interface LocalSerializer<T> {
    val defaultValue: T

    @Throws(IOException::class)
    fun write(outputStream: OutputStream, obj: T & Any)

    @Throws(IOException::class)
    fun read(inputStream: InputStream): T

    fun copy(obj: T & Any): T & Any {
        return ByteArrayOutputStream().use {
            write(it, obj)
            it.toByteArray()
        }.let { ByteArrayInputStream(it) }.use {
            read(it)!!
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
        override val defaultValue: T
            get() = serializer.defaultValue

        final override fun onOpenInputStream(inputStream: InputStream): InputStream {
            return if (serializer is IODecorator) {
                serializer.onOpenInputStream(onOpenInputStream1(inputStream))
            } else {
                onOpenInputStream1(inputStream)
            }
        }

        final override fun onOpenOutStream(outputStream: OutputStream): OutputStream {
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

    open class NoopIODecorator<T>(
        private val serializer: LocalSerializer<T>
    ) : LocalSerializer<T> by serializer, IODecorator {

        override fun onOpenInputStream(inputStream: InputStream): InputStream {
            return if (serializer is IODecorator) {
                serializer.onOpenInputStream(inputStream)
            } else {
                inputStream
            }
        }

        override fun onOpenOutStream(outputStream: OutputStream): OutputStream {
            return if (serializer is IODecorator) {
                serializer.onOpenOutStream(outputStream)
            } else {
                outputStream
            }
        }
    }
}
