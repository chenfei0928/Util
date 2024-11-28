package com.google.protobuf

import android.os.Parcel
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
