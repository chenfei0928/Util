package com.google.protobuf

import androidx.collection.LruCache
import kotlinx.parcelize.Parceler

/**
 * 使ProtobufLite对象支持Parcelable的序列化支持
 * ```
 *   @TypeParceler<InitReply.SandboxVersion?, ProtobufParceler.Instance>()
 *   val remoteSandboxVersion: InitReply.SandboxVersion?,
 * ```
 * [Docs](https://developer.android.com/kotlin/parcelize?hl=zh-cn)
 *
 * 使用反射获取类并进一步获取解析器[Parser]，提供默认实例[ProtobufLiteParceler.Instance]可以直接使用
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-12-21 16:29
 */
open class ProtobufLiteParceler<MessageType : GeneratedMessageLite<MessageType, *>>(
    cacheSize: Int = 10
) : BaseProtobufParceler<MessageType>() {

    private val parserCache = object : LruCache<String, Parser<MessageType>>(cacheSize) {
        override fun create(key: String): Parser<MessageType> {
            @Suppress("UNCHECKED_CAST")
            val messageType = Class.forName(key) as Class<MessageType>
            return messageType.getProtobufLiteParserForType()
        }
    }

    override fun getParser(className: String): Parser<MessageType> {
        return parserCache[className]!!
    }

    @Suppress("UNCHECKED_CAST")
    companion object Instance : Parceler<GeneratedMessageLite<*, *>?>
    by ProtobufLiteParceler() as Parceler<GeneratedMessageLite<*, *>?>
}
