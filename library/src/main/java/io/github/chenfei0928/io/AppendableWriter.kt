package io.github.chenfei0928.io

import androidx.annotation.ReturnThis
import java.io.Flushable
import java.io.Writer

/**
 * @author chenf()
 * @date 2026-01-05 11:51
 */
class AppendableWriter(
    private val target: Appendable,
) : Writer() {
    override fun write(cbuf: CharArray, off: Int, len: Int) {
        target.append(String(cbuf, off, len))
    }

    override fun write(str: String, off: Int, len: Int) {
        target.append(str, off, len)
    }

    override fun write(str: String?) {
        target.append(str)
    }

    override fun write(c: Int) {
        target.append(c.toChar())
    }

    @ReturnThis
    override fun append(
        csq: CharSequence?, start: Int, end: Int
    ): Writer {
        target.append(csq, start, end)
        return this
    }

    @ReturnThis
    override fun append(c: Char): Writer {
        target.append(c)
        return this
    }

    override fun flush() {
        if (target is Flushable) {
            target.flush()
        }
    }

    override fun close() {
        if (target is AutoCloseable) {
            target.close()
        }
    }
}
