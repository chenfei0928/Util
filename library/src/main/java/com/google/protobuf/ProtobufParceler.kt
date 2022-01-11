package com.google.protobuf

import android.os.Parcel
import kotlinx.parcelize.Parceler
import kotlin.reflect.full.createInstance

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
object ProtobufParceler :
    Parceler<GeneratedMessageLite<*, *>?>
    by
    ProtobufParcelerImpl::class.createInstance() as Parceler<GeneratedMessageLite<*, *>?>

private class ProtobufParcelerImpl<MessageType, BuilderType> : Parceler<MessageType?>
        where
MessageType : GeneratedMessageLite<MessageType, BuilderType>,
BuilderType : GeneratedMessageLite.Builder<MessageType, BuilderType> {

    override fun create(parcel: Parcel): MessageType? {
        val className = parcel.readString() ?: return null
        val messageType = Class.forName(className) as Class<MessageType>
        GeneratedMessageLite.getDefaultInstance(messageType)
        val parseFrom = messageType.getParseFrom<MessageType, ByteArray?>()
        return parcel.createByteArray().let(parseFrom)
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
