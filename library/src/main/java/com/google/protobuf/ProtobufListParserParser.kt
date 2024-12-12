package com.google.protobuf

import android.os.Parcel
import android.os.Parcelable.Creator
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.TypeParceler

/**
 * 使Protobuf对象支持Parcelable的序列化支持，需创建子类并传递解析器
 * ```
 *   @TypeParceler<InitReply.SandboxVersion?, SandboxVersionParceler>()
 *   val remoteSandboxVersion: InitReply.SandboxVersion?,
 *
 *   object SandboxVersionParceler : ProtobufListParserParser<SandboxVersion>(SandboxVersion.parser())
 * ```
 * [Docs](https://developer.android.com/kotlin/parcelize?hl=zh-cn)
 *
 * 此类必须abstract，以让使用处创建子类才能被添加到[TypeParceler]注解中使用
 *
 * [Parceler.newArray]不需要重写，由编译器直接在[Creator.createFromParcel]中生成
 *
 * @author chenf()
 * @date 2024-12-12 18:22
 */
open class ProtobufListParserParser<MessageType : MessageLite>(
    private val parser: Parser<MessageType>,
) : Parceler<List<MessageType?>?> {
    constructor(clazz: Class<MessageType>) : this(findProtobufParser(clazz)!!)

    final override fun create(parcel: Parcel): List<MessageType?>? {
        val size = parcel.readInt()
        return if (size < 0) {
            null
        } else (0 until size).map {
            parser.parseFrom(parcel.createByteArray())
        }
    }

    final override fun List<MessageType?>?.write(
        parcel: Parcel, flags: Int
    ) = if (this == null) {
        parcel.writeInt(-1)
    } else {
        parcel.writeInt(size)
        forEach {
            parcel.writeByteArray(it?.toByteArray())
        }
    }
}
