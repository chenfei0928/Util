package io.github.chenfei0928.repository.local

import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.Parser
import com.google.protobuf.getProtobufLiteDefaultInstance
import com.google.protobuf.getProtobufLiteParserForType
import java.io.InputStream
import java.io.OutputStream

/**
 * 用于ProtobufLite的数据序列化
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-22 16:03
 */
class ProtobufLiteSerializer<MessageType, BuilderType>(
    messageType: Class<MessageType>
) : LocalSerializer<MessageType>
        where
MessageType : GeneratedMessageLite<MessageType, BuilderType>,
BuilderType : GeneratedMessageLite.Builder<MessageType, BuilderType> {

    private val parser: Parser<MessageType> = messageType.getProtobufLiteParserForType()

    override val defaultValue: MessageType = messageType.getProtobufLiteDefaultInstance()

    override fun write(outputStream: OutputStream, obj: MessageType) {
        obj.writeTo(outputStream)
        outputStream.flush()
    }

    override fun read(inputStream: InputStream): MessageType {
        return parser.parseFrom(inputStream)
    }

    override fun copy(obj: MessageType): MessageType {
        return obj.toBuilder().build()
    }
}
