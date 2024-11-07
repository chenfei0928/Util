package com.google.protobuf

import android.os.Parcel
import android.os.Parcelable
import androidx.collection.LruCache
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.TypeParceler

/**
 * 使Protobuf对象支持Parcelable的序列化支持，需创建子类并传递解析器
 * ```
 *   @TypeParceler<InitReply.SandboxVersion?, SandboxVersionParceler>()
 *   val remoteSandboxVersion: InitReply.SandboxVersion?,
 *
 *   object SandboxVersionParceler : ProtobufParceler<SandboxVersion>(SandboxVersion.parser())
 * ```
 * [Docs](https://developer.android.com/kotlin/parcelize?hl=zh-cn)
 *
 * 此类必须abstract，以让使用处创建子类才能被添加到[TypeParceler]注解中使用
 *
 * [Parceler.newArray]不需要重写，由编译器直接在[Parcelable.Creator.createFromParcel]中生成
 *
 * @param MessageType
 * @property parser
 * @constructor Create empty Protobuf parceler
 */
abstract class ProtobufParceler<MessageType : MessageLite>(
    private val parser: Parser<MessageType>,
) : Parceler<MessageType?> {

    final override fun create(parcel: Parcel): MessageType? {
        return parcel.createByteArray()?.let(parser::parseFrom)
    }

    final override fun MessageType?.write(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(this?.toByteArray())
    }
}

/**
 * 使Protobuf对象支持Parcelable的序列化支持
 * 通过写入并读取实例类名，并进一步通过[getParser]方法获取[Parser]解析器
 *
 * [Docs](https://developer.android.com/kotlin/parcelize?hl=zh-cn)
 *
 * [Parceler.newArray]不需要重写，由编译器直接在[Parcelable.Creator.createFromParcel]中生成
 *
 * @param MessageType
 * @constructor Create empty Base protobuf parceler
 */
abstract class BaseProtobufParceler<MessageType : MessageLite> : Parceler<MessageType?> {
    protected abstract fun getParser(className: String): Parser<MessageType>

    final override fun create(parcel: Parcel): MessageType? {
        val className = parcel.readString()
            ?: return null
        val parser = getParser(className)
        return parcel.createByteArray().let(parser::parseFrom)
    }

    final override fun MessageType?.write(parcel: Parcel, flags: Int) {
        if (this == null) {
            parcel.writeString(null)
        } else {
            parcel.writeString(this.javaClass.name)
            parcel.writeByteArray(this.toByteArray())
        }
    }
}

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
