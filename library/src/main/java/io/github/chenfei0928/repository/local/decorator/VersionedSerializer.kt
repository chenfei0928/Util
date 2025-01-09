package io.github.chenfei0928.repository.local.decorator

import io.github.chenfei0928.lang.toByteArray
import io.github.chenfei0928.lang.toLong
import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.InputStream
import java.io.OutputStream

/**
 * 弥补某些数据结构或序列化实现在跨代码版本序列化和反序列化时不兼容的问题
 * 进行版本校验，校验失败直接删除缓存文件
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-05 15:20
 */
class VersionedSerializer<T : Any>
private constructor(
    serializer: LocalSerializer<T>,
    versionCodeLong: Long
) : LocalSerializer.BaseIODecorator<T>(serializer) {
    private val versionCode: ByteArray = versionCodeLong.toByteArray()

    override fun wrapInputStream(inputStream: InputStream): InputStream {
        // long 类型8字节
        val savedVersionCode = ByteArray(Long.SIZE_BYTES)
        // 读取本地保存的内容的数据结构版本号
        inputStream.read(savedVersionCode)
        require(versionCode contentEquals savedVersionCode) {
            // 抛出异常，通知对数据进行反序列化失败，交由调用处LocalFileModule删除缓存文件
            "当前版本是${versionCode.toLong()}, 本地文件的版本是${savedVersionCode.toLong()}，版本不匹配！数据结构可能已经被修改"
        }
        // 版本号校验一致，读取内容
        return inputStream
    }

    override fun wrapOutputStream(outputStream: OutputStream): OutputStream {
        // 写入应用版本号
        outputStream.write(versionCode)
        // 将数据结构版本号写入io流
        return outputStream
    }

    companion object {
        fun <T : Any> LocalSerializer<T>.versioned(
            versionCodeInt: Long
        ): LocalSerializer<T> = if (this is VersionedSerializer) {
            this
        } else {
            VersionedSerializer(this, versionCodeInt)
        }
    }
}
