package io.github.chenfei0928.io

import java.io.OutputStream

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-12 14:36
 */
class MultiTargetOutputStream(
    private val target: Array<OutputStream>
) : OutputStream() {

    override fun write(b: Int) {
        target.forEach {
            it.write(b)
        }
    }

    override fun write(b: ByteArray) {
        target.forEach {
            it.write(b)
        }
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        target.forEach {
            it.write(b, off, len)
        }
    }

    override fun flush() {
        target.forEach {
            it.flush()
        }
    }

    override fun close() {
        target.forEach {
            it.close()
        }
    }
}
