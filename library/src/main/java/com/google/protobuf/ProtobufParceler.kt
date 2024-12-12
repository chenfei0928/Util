package com.google.protobuf

import android.os.Parcel
import android.os.Parcelable.Creator
import androidx.collection.LruCache
import kotlinx.parcelize.Parceler

/**
 * 使Protobuf对象支持Parcelable的序列化支持
 * 通过写入并读取实例类名，并进一步通过[parserCache]获取[Parser]解析器
 * ```
 *   @TypeParceler<InitReply.SandboxVersion?, ProtobufParceler.Instance>()
 *   val remoteSandboxVersion: InitReply.SandboxVersion?,
 * ```
 *
 * [Docs](https://developer.android.com/kotlin/parcelize?hl=zh-cn)
 *
 * 使用反射获取类并进一步获取解析器[Parser]，提供默认实例[ProtobufParceler.Instance]可以直接使用
 *
 * [Parceler.newArray]不需要重写，由编译器直接在[Creator.createFromParcel]中生成
 *
 * @param MessageType
 * @constructor Create empty Base protobuf parceler
 */
class ProtobufParceler<MessageType : MessageLite>(
    cacheSize: Int = 10
) : Parceler<MessageType?> {
    private val parserCache = object : LruCache<String, Parser<MessageType>>(cacheSize) {
        @Suppress("UNCHECKED_CAST")
        override fun create(key: String): Parser<MessageType> {
            val messageType = Class.forName(key) as Class<MessageType>
            return findProtobufParser(messageType)!!
        }
    }

    override fun create(parcel: Parcel): MessageType? {
        val className = parcel.readString()
            ?: return null
        val parser = parserCache[className]!!
        return parcel.createByteArray().let(parser::parseFrom)
    }

    override fun MessageType?.write(parcel: Parcel, flags: Int) {
        if (this == null) {
            parcel.writeString(null)
        } else {
            parcel.writeString(this.javaClass.name)
            parcel.writeByteArray(this.toByteArray())
        }
    }

    companion object Instance : Parceler<MessageLite?> by ProtobufParceler()
}
