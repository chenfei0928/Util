package io.github.chenfei0928.repository.local

import java.io.InputStream
import java.io.OutputStream

/**
 * 数据保质期序列化
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-10 09:35
 */
abstract class BaseExpirationDateSerializer<T>(
    private val serializer: LocalSerializer<T>
) : LocalSerializer<T> by serializer {

    override fun save(outputStream: OutputStream, obj: T) {
        // 记录保存时间
        val currentTimeMillis = System.currentTimeMillis()
        outputStream.write((currentTimeMillis shr 32).toInt())
        outputStream.write(currentTimeMillis.toInt())
        serializer.save(outputStream, obj)
    }

    override fun load(inputStream: InputStream): T? {
        // long 类型8字节
        val savedVersionCode = ByteArray(8)
        // 读取本地保存的内容的数据结构版本号
        inputStream.read(savedVersionCode)
        if (check(savedVersionCode.toLong())) {
            // 版本号校验一致，读取内容
            return serializer.load(inputStream)
        } else {
            // 抛出异常，通知对数据进行反序列化失败，交由调用处LocalFileModule删除缓存文件
            throw IllegalArgumentException(
                "本地文件的标记时间是${savedVersionCode.toLong()}，数据已过期"
            )
        }
    }

    abstract fun check(localSavedTimeMillis: Long): Boolean

    private fun ByteArray.toLong(): Long {
        var count = 0L
        forEach {
            count = count shl 8
            count = count or it.toLong()
        }
        return count
    }
}

class ExpirationDateSerializer<T>(
    serializer: LocalSerializer<T>,
    private val timeout: Long
) : BaseExpirationDateSerializer<T>(serializer) {

    override fun check(localSavedTimeMillis: Long): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        return localSavedTimeMillis + timeout >= currentTimeMillis
    }
}
