package com.chenfei.module

import java.io.InputStream
import java.io.OutputStream

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-20 16:28
 */
class StringSerializer : LocalSerializer<String> {
    override fun save(outputStream: OutputStream, obj: String) {
        outputStream.write(obj.toByteArray())
    }

    override fun load(inputStream: InputStream): String? {
        return String(inputStream.readBytes())
    }
}
