package io.github.chenfei0928.repository.local.serializer

import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.InputStream
import java.io.OutputStream

/**
 * 字符串序列化支持
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-20 16:28
 */
object StringSerializer : LocalSerializer<String> {
    override val defaultValue: String = ""

    override fun write(outputStream: OutputStream, obj: String) {
        outputStream.write(obj.toByteArray())
    }

    override fun read(inputStream: InputStream): String {
        return String(inputStream.readBytes())
    }

    override fun copy(obj: String): String {
        return obj
    }

    override fun toString(): String {
        return "StringSerializer"
    }
}
