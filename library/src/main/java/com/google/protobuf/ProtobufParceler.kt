package com.google.protobuf

import android.os.Parcel
import android.os.Parcelable.Creator
import androidx.collection.LruCache
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.TypeParceler

/**
 * 使 `Protobuf` 对象支持Parcelable的序列化支持
 *
 * 根据 [parser] 是否传入，进行序列化时的行为可能会不同：
 * - 传入了 [parser] 时：在序列化时不会写入类型信息
 * - 没有传入 [parser] 时：会写入类型（但连续的多个相同类型的元素时只有第一次会写入类型信息）
 *
 * ```
 * // 不会写入类型信息
 * @TypeParceler<ProtoBean?, ProtoBeanParceler>()
 * val protoBean: ProtoBean?,
 * // 会写入类型信息
 * @TypeParceler<ProtoBean?, ProtobufListParceler.Instance>()
 * val protoBean: ProtoBean?,
 *
 * object ProtoBeanParceler : ProtobufListParserParser<ProtoBean?>(ProtoBean.parser())
 * ```
 * [Docs](https://developer.android.com/kotlin/parcelize?hl=zh-cn)
 *
 * 此类设置为open，以让使用处创建单例类才能被添加到[TypeParceler]注解中使用
 *
 * [Parceler.newArray]不需要重写，由编译器直接在[Creator.createFromParcel]中生成
 *
 * [Docs](https://developer.android.com/kotlin/parcelize?hl=zh-cn)
 *
 * @param MessageType
 * @constructor Create empty Base protobuf parceler
 */
open class ProtobufParceler<MessageType : MessageLite>(
    private val parser: Parser<MessageType>? = null,
    cacheSize: Int = 10,
) : LruCache<String, Parser<MessageType>>(cacheSize), Parceler<MessageType?> {

    override fun create(parcel: Parcel): MessageType? {
        return if (parser != null) {
            parcel.createByteArray()?.let(parser::parseFrom)
        } else {
            val className = parcel.readString()
                ?: return null
            val parser = this[className]!!
            parcel.createByteArray().let(parser::parseFrom)
        }
    }

    override fun MessageType?.write(parcel: Parcel, flags: Int) {
        if (parser != null) {
            parcel.writeByteArray(this?.toByteArray())
        } else if (this == null) {
            parcel.writeString(null)
        } else {
            parcel.writeString(this.javaClass.name)
            parcel.writeByteArray(this.toByteArray())
        }
    }

    override fun create(key: String): Parser<MessageType> {
        @Suppress("UNCHECKED_CAST")
        val messageType = Class.forName(key) as Class<MessageType>
        return messageType.protobufParserForType!!
    }

    companion object Instance : Parceler<MessageLite?> by ProtobufParceler()
}
