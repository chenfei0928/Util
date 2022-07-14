package io.github.chenfei0928.repository.datastore

import androidx.datastore.core.Serializer
import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.InputStream
import java.io.OutputStream

/**
 * @author chenfei()
 * @date 2022-06-20 15:10
 */
fun <T : Any> LocalSerializer<T>.toDatastore() = object : Serializer<T> {
    override val defaultValue: T
        get() = this@toDatastore.defaultValue

    override suspend fun readFrom(input: InputStream): T {
        return this@toDatastore.read(input)
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        this@toDatastore.write(output, t)
    }
}
