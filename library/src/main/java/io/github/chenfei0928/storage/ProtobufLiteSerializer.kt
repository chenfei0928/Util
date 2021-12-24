package io.github.chenfei0928.storage

import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.getParseFrom
import java.io.InputStream
import java.io.OutputStream

/**
 * 用于ProtobufLite的数据序列化
 *
 * @author chenfei(chenfei@cocos.com)
 * @date 2021-11-22 16:03
 */
class ProtobufLiteSerializer<MessageType, BuilderType>(
    clazz: Class<MessageType>
) : LocalSerializer<MessageType>
        where
MessageType : GeneratedMessageLite<MessageType, BuilderType>,
BuilderType : GeneratedMessageLite.Builder<MessageType, BuilderType> {

    private val parser = clazz.getParseFrom<MessageType, Any>()

    override fun save(outputStream: OutputStream, obj: MessageType) {
        obj.writeTo(outputStream)
        outputStream.flush()
    }

    override fun load(inputStream: InputStream): MessageType {
        return parser(inputStream)
    }

    override fun copy(obj: MessageType): MessageType {
        return obj.toBuilder().build()
    }
}
