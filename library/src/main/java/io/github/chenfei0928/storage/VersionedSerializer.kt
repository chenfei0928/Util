package io.github.chenfei0928.storage

import java.io.InputStream
import java.io.OutputStream

/**
 * 弥补某些数据结构或序列化实现在跨代码版本序列化和反序列化时不兼容的问题
 * 进行版本校验，校验失败直接删除缓存文件
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-05 15:20
 */
class VersionedSerializer<T>(
    private val serializer: LocalSerializer<T>, versionCodeLong: Long
) : LocalSerializer<T> by serializer {
    private val versionCode: ByteArray = versionCodeLong.run {
        byteArrayOf(
                (this shr 56).toByte(),
                (this shr 48).toByte(),
                (this shr 40).toByte(),
                (this shr 32).toByte(),
                (this shr 24).toByte(),
                (this shr 16).toByte(),
                (this shr 8).toByte(),
                this.toByte()
        )
    }

    override fun save(outputStream: OutputStream, obj: T) {
        // 写入应用版本号
        outputStream.write(versionCode)
        // 将数据结构版本号写入io流
        serializer.save(outputStream, obj)
    }

    override fun load(inputStream: InputStream): T? {
        // long 类型8字节
        val savedVersionCode = ByteArray(8)
        // 读取本地保存的内容的数据结构版本号
        inputStream.read(savedVersionCode)
        if (versionCode contentEquals savedVersionCode) {
            // 版本号校验一致，读取内容
            return serializer.load(inputStream)
        } else {
            // 抛出异常，通知对数据进行反序列化失败，交由调用处LocalFileModule删除缓存文件
            throw IllegalArgumentException(
                    "当前版本是${versionCode.toLong()}, 本地文件的版本是${savedVersionCode.toLong()}，版本不匹配！数据结构可能已经被修改"
            )
        }
    }

    private fun ByteArray.toLong(): Long {
        var count = 0L
        forEach {
            count shl 8
            count = count or it.toLong()
        }
        return count
    }
}

fun <T> LocalSerializer<T>.versioned(versionCodeInt: Long): VersionedSerializer<T> =
        if (this is VersionedSerializer) {
            this
        } else {
            VersionedSerializer(this, versionCodeInt)
        }
