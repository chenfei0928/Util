package io.github.chenfei0928.repository.local.decorator

import io.github.chenfei0928.lang.toLong
import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.InputStream
import java.io.OutputStream

/**
 * 对数据添加数据保质期序列化
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-10 09:35
 */
abstract class BaseExpirationDateSerializer<T : Any>(
    serializer: LocalSerializer<T>
) : LocalSerializer.BaseIODecorator<T>(serializer) {

    final override fun wrapInputStream(inputStream: InputStream): InputStream {
        // long 类型8字节
        val savedVersionCode = ByteArray(Long.SIZE_BYTES)
        // 读取本地保存的内容的数据结构版本号
        inputStream.read(savedVersionCode)
        // 版本号校验一致，读取内容
        require(check(savedVersionCode.toLong())) {
            // 抛出异常，通知对数据进行反序列化失败，交由调用处LocalFileModule删除缓存文件
            "本地文件的标记时间是${savedVersionCode.toLong()}，数据已过期"
        }
        // 版本号校验一致，读取内容
        return inputStream
    }

    final override fun wrapOutputStream(outputStream: OutputStream): OutputStream {
        // 记录保存时间
        val currentTimeMillis = System.currentTimeMillis()
        outputStream.write((currentTimeMillis shr Int.SIZE_BITS).toInt())
        outputStream.write(currentTimeMillis.toInt())
        return outputStream
    }

    protected abstract fun check(localSavedTimeMillis: Long): Boolean
}
