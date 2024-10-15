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
    interface IODecorator<T> : LocalSerializer<T> {
        @Throws(IOException::class)
        fun onOpenInputStream(inputStream: InputStream): InputStream

        @Throws(IOException::class)
        fun onOpenOutStream(outputStream: OutputStream): OutputStream
    }

    /**
     * IO装饰器，实现对子序列化器是否是装饰器的判断
     *
     * 子类重写[wrapOutputStream]与[wrapInputStream]即可对io流进行包装
     *
     * 如果子类需要对数据进行添加校验头，也是重写该方法并在实际写入/读取前处理校验数据并直接返回入参
     */
    abstract class BaseIODecorator<T>(
        private val serializer: LocalSerializer<T>
    ) : LocalSerializer<T> by serializer, IODecorator<T> {

        final override fun onOpenInputStream(inputStream: InputStream): InputStream {
            return if (serializer is IODecorator) {
                // 先自己将输入流包装后传入子装饰器
                serializer.onOpenInputStream(wrapInputStream(inputStream))
            } else {
                wrapInputStream(inputStream)
            }
        }

        final override fun onOpenOutStream(outputStream: OutputStream): OutputStream {
            return if (serializer is IODecorator) {
                // 先自己将输出流包装后传入子装饰器
                serializer.onOpenOutStream(wrapOutputStream(outputStream))
            } else {
                wrapOutputStream(outputStream)
            }
        }

        @Throws(IOException::class)
        abstract fun wrapInputStream(inputStream: InputStream): InputStream

        @Throws(IOException::class)
        abstract fun wrapOutputStream(outputStream: OutputStream): OutputStream
    }
}
