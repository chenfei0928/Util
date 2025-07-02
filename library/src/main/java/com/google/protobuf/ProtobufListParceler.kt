package com.google.protobuf

import android.os.Parcel
import android.os.Parcelable.Creator
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.TypeParceler

/**
 * 使 `List<Protobuf>` 对象支持Parcelable的序列化支持
 *
 * 根据 [parser] 是否传入，进行序列化时的行为可能会不同：
 * - 传入了 [parser] 时：List 每个元素都是该消息类型，并在序列化时不会写入类型信息
 * - 没有传入 [parser] 时：会写入每个元素的类型（但连续的多个相同类型的元素时只有第一次会写入类型信息）
 *
 * ```
 * // 不会写入类型信息
 * @TypeParceler<List<ProtoBean?>?, ProtoBeansParceler>()
 * val protoBeans: List<ProtoBean?>?,
 * // 会写入类型信息
 * @TypeParceler<List<ProtoBean?>?, ProtobufListParceler.Instance>()
 * val protoBeans: List<ProtoBean?>?,
 *
 * object ProtoBeansParceler : ProtobufListParserParser<List<ProtoBean?>?>(ProtoBean.parser())
 * ```
 *
 * 此类设置为open，以让使用处创建单例类才能被添加到[TypeParceler]注解中使用
 *
 * [Parceler.newArray]不需要重写，由编译器直接在[Creator.createFromParcel]中生成
 *
 * [Docs](https://developer.android.com/kotlin/parcelize?hl=zh-cn)
 *
 * @author chenf()
 * @date 2024-12-12 16:56
 */
open class ProtobufListParceler<MessageType : MessageLite> : Parceler<List<MessageType?>?> {
    private val parser: Parser<MessageType>?

    private constructor() {
        this.parser = null
    }

    constructor(parser: Parser<MessageType>) {
        this.parser = parser
    }

    override fun create(parcel: Parcel): List<MessageType?>? {
        val size = parcel.readInt()
        return if (size < 0) {
            null
        } else if (parser != null) {
            // 有传入反序列化器，直接根据读取到的内容长度反序列化每一条数据
            (0 until size).map { parser.parseFrom(parcel.createByteArray()) }
        } else {
            // 没有传入反序列化器，需要读取数据类型信息，以支持不同消息类型的并存
            var parser: Parser<MessageType>? = null
            (0 until size).map {
                // 根据读取到的数据大小遍历，先读取每个元素的数据类型
                when (val itClassName = parcel.readString()) {
                    null -> null
                    "" -> {
                        // 与前一条数据类型一致，直接读取二进制内容并反序列化
                        parser!!.parseFrom(parcel.createByteArray())
                    }
                    else -> {
                        // 有写入数据类型获取其反序列化器后进行反序列化
                        parser = MessageParserCache.getParser(itClassName)
                        parser.parseFrom(parcel.createByteArray())
                    }
                }
            }
        }
    }

    override fun List<MessageType?>?.write(
        parcel: Parcel, flags: Int
    ) = if (this == null) {
        parcel.writeInt(-1)
    } else if (parser != null) {
        // 有传入反序列化器，不需要写入数据类型信息，直接写入大小与每一条数据
        parcel.writeInt(size)
        forEach {
            parcel.writeByteArray(it?.toByteArray())
        }
    } else {
        // 没有传入反序列化器，需要写入数据类型信息，以支持不同消息类型的并存
        // 写入数据大小，然后是每个元素的数据类型（第一次写入时），最后才是该元素的二进制内容
        parcel.writeInt(size)
        var lastClass: Class<MessageType>? = null
        forEach {
            when (val itClass = it?.javaClass) {
                null -> parcel.writeString(null)
                lastClass -> {
                    // 与前一条数据类型一致，不写入数据类型信息
                    parcel.writeString("")
                    parcel.writeByteArray(it.toByteArray())
                }
                else -> {
                    // 与前一条数据类型不一致，写入数据类型信息
                    lastClass = itClass
                    parcel.writeString(itClass.name)
                    parcel.writeByteArray(it.toByteArray())
                }
            }
        }
    }

    override fun toString(): String {
        return if (parser == null) {
            "ProtobufListParceler"
        } else {
            "ProtobufListParceler(parser=$parser)"
        }
    }

    companion object Instance : Parceler<List<MessageLite?>?> by ProtobufListParceler() {

        inline operator fun <reified MessageType : MessageLite> invoke(): Parceler<List<MessageType?>?> =
            ProtobufListParceler(MessageType::class.java.protobufParserForType)
    }
}
