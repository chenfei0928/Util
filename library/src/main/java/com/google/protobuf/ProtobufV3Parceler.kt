package com.google.protobuf

import androidx.collection.LruCache
import kotlinx.parcelize.Parceler

/**
 * 使Protobuf对象支持Parcelable的序列化支持
 * ```
 *   @TypeParceler<InitReply.SandboxVersion?, ProtobufParceler.Instance>()
 *   val remoteSandboxVersion: InitReply.SandboxVersion?,
 * ```
 * [Docs](https://developer.android.com/kotlin/parcelize?hl=zh-cn)
 *
 * 使用反射获取类并进一步获取解析器[Parser]，提供默认实例[ProtobufV3Parceler.Instance]可以直接使用
 *
 * @author chenf()
 * @date 2023-03-28 14:27
 */
open class ProtobufV3Parceler<MessageType : GeneratedMessageV3>(
    cacheSize: Int = 10
) : BaseProtobufParceler<MessageType>() {

    private val parserCache = object : LruCache<String, Parser<MessageType>>(cacheSize) {
        override fun create(key: String): Parser<MessageType> {
            @Suppress("UNCHECKED_CAST")
            val messageType = Class.forName(key) as Class<MessageType>
            return messageType.getProtobufV3ParserForType()
        }
    }

    override fun getParser(className: String): Parser<MessageType> {
        return parserCache[className]!!
    }

    companion object Instance : Parceler<GeneratedMessageV3?> by ProtobufV3Parceler()
}
