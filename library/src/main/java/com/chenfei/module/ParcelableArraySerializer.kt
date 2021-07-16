package com.chenfei.module

import android.os.Parcelable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * 对[Parcelable]数组进行序列化和反序列化
 * 由于其API在开始时不是为持久化数据设计，会导致其数据结构定义变化后反序列化时大概率会产生数据混乱的问题
 */
internal class ParcelableArraySerializer<T : Parcelable>(
    private val creator: Parcelable.Creator<T>
) : LocalSerializer<Array<T>> {

    @Throws(IOException::class)
    override fun save(outputStream: OutputStream, obj: Array<T>) {
        outputStream.write(ParcelableUtils.marshallArray(obj))
        outputStream.flush()
    }

    @Throws(IOException::class)
    override fun load(inputStream: InputStream): Array<T>? {
        return inputStream.readBytes().let {
            ParcelableUtils.unmarshallArray(it, creator)
        }
    }
}
