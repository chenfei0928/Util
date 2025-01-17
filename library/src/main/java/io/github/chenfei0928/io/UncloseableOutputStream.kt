package io.github.chenfei0928.io

import java.io.FileOutputStream
import java.io.OutputStream

/**
 * copy form [androidx.datastore.core.UncloseableOutputStream]
 */
class UncloseableOutputStream(val fileOutputStream: FileOutputStream) : OutputStream() {

    override fun write(b: Int) {
        fileOutputStream.write(b)
    }

    override fun write(b: ByteArray) {
        fileOutputStream.write(b)
    }

    override fun write(bytes: ByteArray, off: Int, len: Int) {
        fileOutputStream.write(bytes, off, len)
    }

    override fun close() {
        // We will not close the underlying FileOutputStream until after we're done syncing
        // the fd. This is useful for things like b/173037611.
    }

    override fun flush() {
        fileOutputStream.flush()
    }
}
