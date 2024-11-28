package com.google.protobuf

import android.os.Parcel
import kotlinx.parcelize.Parceler

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
