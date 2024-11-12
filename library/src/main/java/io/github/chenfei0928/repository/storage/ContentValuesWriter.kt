package io.github.chenfei0928.repository.storage

import android.os.Bundle
import androidx.fragment.app.Fragment
import java.io.IOException
import java.io.OutputStream

interface ContentValuesWriter {
    fun parseArg(host: Fragment, arg: Bundle?): Boolean {
        return true
    }

    @Throws(IOException::class)
    fun write(outputStream: OutputStream)
}
