package com.google.protobuf

import android.os.Parcel
import androidx.collection.LruCache
import kotlinx.parcelize.Parceler

abstract class BaseProtobufParceler<MessageType : MessageLite> : Parceler<MessageType?> {

    protected abstract val parserCache: LruCache<String, Parser<MessageType>>

    override fun create(parcel: Parcel): MessageType? {
        val className = parcel.readString() ?: return null
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
}

/**
 * @author chenf()
 * @date 2023-03-28 14:27
 */
open class ProtobufParceler<MessageType : GeneratedMessageV3>
    : BaseProtobufParceler<MessageType>() {

    override val parserCache = object : LruCache<String, Parser<MessageType>>(10) {
        override fun create(key: String): Parser<MessageType> {
            val messageType = Class.forName(key) as Class<MessageType>
            return messageType.getProtobufParserForType()
        }
    }

    companion object Instance : Parceler<GeneratedMessageV3?>
    by ProtobufParceler::class.java.getDeclaredConstructor()
        .newInstance() as Parceler<GeneratedMessageV3?>
}

/**
 * 使Protobuf对象支持Parcelable的序列化支持
 * ```
 *   @TypeParceler<InitReply.SandboxVersion?, ProtobufParceler>()
 *   val remoteSandboxVersion: InitReply.SandboxVersion?,
 * ```
 * [Docs](https://developer.android.com/kotlin/parcelize?hl=zh-cn)
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-12-21 16:29
 */
open class ProtobufLiteParceler<MessageType, BuilderType> : BaseProtobufParceler<MessageType>()
        where
MessageType : GeneratedMessageLite<MessageType, BuilderType>,
BuilderType : GeneratedMessageLite.Builder<MessageType, BuilderType> {

    override val parserCache = object : LruCache<String, Parser<MessageType>>(10) {
        override fun create(key: String): Parser<MessageType> {
            val messageType = Class.forName(key) as Class<MessageType>
            return messageType.getProtobufLiteParserForType()
        }
    }

    companion object Instance : Parceler<GeneratedMessageLite<*, *>?>
    by ProtobufLiteParceler::class.java.getDeclaredConstructor()
        .newInstance() as Parceler<GeneratedMessageLite<*, *>?>
}
