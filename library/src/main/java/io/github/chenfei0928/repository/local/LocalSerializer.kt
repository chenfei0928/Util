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
interface LocalSerializer<T : Any> {
    val defaultValue: T

    @Throws(IOException::class)
    fun write(outputStream: OutputStream, obj: T)

    @Throws(IOException::class)
    fun read(inputStream: InputStream): T

    fun copy(obj: T): T {
        return ByteArrayOutputStream().use {
            write(it, obj)
            it.toByteArray()
        }.let { ByteArrayInputStream(it) }.use {
            read(it)
        }
    }
}
