package com.chenfei.module

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

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
}
