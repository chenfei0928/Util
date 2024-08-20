/**
 * 数据Base64化修饰器
 *
 * @author chenfei(chenfei@gmail.com)
 * @date 2022-01-12 10:25
 */
package io.github.chenfei0928.repository.local

import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * 数据Base64化修饰器
 *
 * @author chenfei(chenfei@gmail.com)
 * @date 2022-01-12 10:25
 */
class Base64Serializer<T>
private constructor(
    serializer: LocalSerializer<T>
) : LocalSerializer.BaseIODecorator<T>(serializer) {
    override fun onOpenInputStream1(inputStream: InputStream): InputStream {
        return Base64InputStream(inputStream, Base64.DEFAULT)
    }

    override fun onOpenOutStream1(outputStream: OutputStream): OutputStream {
        return Base64OutputStream(outputStream, Base64.DEFAULT)
    }

    companion object {
        /**
         * 数据Base64化修饰器
         */
        fun <T> LocalSerializer<T>.base64(): LocalSerializer<T> = if (this is Base64Serializer) {
            this
        } else {
            Base64Serializer(this)
        }
    }
}
