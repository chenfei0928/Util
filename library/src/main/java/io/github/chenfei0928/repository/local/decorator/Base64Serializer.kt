/**
 * 数据Base64化修饰器
 *
 * @author chenfei(chenfei@gmail.com)
 * @date 2022-01-12 10:25
 */
package io.github.chenfei0928.repository.local.decorator

import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.InputStream
import java.io.OutputStream

/**
 * 数据Base64化修饰器
 *
 * @author chenfei(chenfei@gmail.com)
 * @date 2022-01-12 10:25
 */
class Base64Serializer<T : Any>
private constructor(
    private val serializer: LocalSerializer<T>,
    private val base64Flag: Int = Base64.DEFAULT
) : LocalSerializer<T> by serializer {

    override fun read(inputStream: InputStream): T {
        return Base64InputStream(inputStream, base64Flag).use {
            serializer.read(it)
        }
    }

    override fun write(outputStream: OutputStream, obj: T) {
        Base64OutputStream(outputStream, base64Flag).use {
            serializer.write(it, obj)
            it.flush()
        }
    }

    override fun toString(): String {
        return "Base64Serializer(serializer=$serializer)"
    }

    companion object {
        /**
         * 数据Base64化修饰器
         */
        fun <T : Any> LocalSerializer<T>.base64(): LocalSerializer<T> =
            this as? Base64Serializer ?: Base64Serializer(this)
    }
}
