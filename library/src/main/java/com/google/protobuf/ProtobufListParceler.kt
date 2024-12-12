package com.google.protobuf

import android.os.Parcel
import android.os.Parcelable.Creator
import androidx.collection.LruCache
import kotlinx.parcelize.Parceler

/**
 * 使List<Protobuf>对象支持Parcelable的序列化支持
 * 通过写入并读取实例类名，并进一步通过[parserCache]获取[Parser]解析器
 *
 * [Docs](https://developer.android.com/kotlin/parcelize?hl=zh-cn)
 *
 * [Parceler.newArray]不需要重写，由编译器直接在[Creator.createFromParcel]中生成
 *
 * @author chenf()
 * @date 2024-12-12 16:56
 */
class ProtobufListParceler<MessageType : MessageLite>(
    cacheSize: Int = 10
) : Parceler<List<MessageType?>?> {
    private val parserCache = object : LruCache<String, Parser<MessageType>>(cacheSize) {
        @Suppress("UNCHECKED_CAST")
        override fun create(key: String): Parser<MessageType> {
            val messageType = Class.forName(key) as Class<MessageType>
            return getProtobufParser(messageType)!!
        }
    }

    override fun create(parcel: Parcel): List<MessageType?>? {
        val size = parcel.readInt()
        var lastClassName: String? = null
        return if (size < 0) {
            null
        } else (0 until size).map {
            val parser = when (val itClassName = parcel.readString()) {
                null -> null
                "" -> parserCache[lastClassName!!]!!
                else -> {
                    lastClassName = itClassName
                    parserCache[itClassName]!!
                }
            }
            parser?.parseFrom(parcel.createByteArray())
        }
    }

    override fun List<MessageType?>?.write(
        parcel: Parcel, flags: Int
    ) = if (this == null) {
        parcel.writeInt(-1)
    } else {
        parcel.writeInt(size)
        var lastClass: Class<MessageType>? = null
        forEach {
            val itClassName = when (val itClass = it?.javaClass) {
                null -> null
                lastClass -> ""
                else -> {
                    lastClass = itClass
                    itClass.name
                }
            }
            parcel.writeString(itClassName)
            if (it != null) {
                parcel.writeByteArray(it.toByteArray())
            }
        }
    }

    companion object Instance : Parceler<List<MessageLite?>?> by ProtobufListParceler<MessageLite>()
}
