package io.github.chenfei0928.io

import java.io.InputStream
import java.io.OutputStream

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-12 13:54
 */
class IoRedirectThread(
    private val source: InputStream,
    private val target: OutputStream,
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE
) : Thread() {
    init {
        name = "IoRedirectThread-$id"
    }

    override fun run() {
        source.copyTo(target, bufferSize)
    }
}
