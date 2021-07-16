package com.chenfei.io

import java.io.InputStream
import java.io.OutputStream

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-12 13:54
 */
class IoRedirectThread(
    private val source: InputStream, private val target: OutputStream, bufferSize: Int = 8192
) : Thread() {
    private val buffer = ByteArray(bufferSize)

    override fun run() {
        source.use {
            target.use {
                var size = source.read(buffer)
                while (size > 0) {
                    target.write(buffer, 0, size)
                    size = source.read(buffer)
                }
            }
        }
    }
}
