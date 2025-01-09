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
    serializer: LocalSerializer<T>
) : LocalSerializer.BaseIODecorator<T>(serializer) {
    override fun wrapInputStream(inputStream: InputStream): InputStream {
        return Base64InputStream(inputStream, Base64.DEFAULT)
    }

    override fun wrapOutputStream(outputStream: OutputStream): OutputStream {
        return Base64OutputStream(outputStream, Base64.DEFAULT)
    }

    companion object {
        /**
         * 数据Base64化修饰器
         */
        fun <T : Any> LocalSerializer<T>.base64(): LocalSerializer<T> =
            if (this is Base64Serializer) this else Base64Serializer(this)
    }
}
