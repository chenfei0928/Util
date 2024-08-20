/**
 * 用于Protobuf的数据序列化
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-22 16:03
 */
package io.github.chenfei0928.repository.local

import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import com.google.protobuf.getProtobufLiteDefaultInstance
import java.io.InputStream
import java.io.OutputStream

abstract class BaseProtobufSerializer<MessageType : MessageLite>
    : LocalSerializer<MessageType> {
    protected abstract val parser: Parser<MessageType>

    override fun write(outputStream: OutputStream, obj: MessageType) {
        obj.writeTo(outputStream)
        outputStream.flush()
    }

    override fun read(inputStream: InputStream): MessageType {
        return parser.parseFrom(inputStream)
    }

    override fun copy(obj: MessageType): MessageType {
        return obj.toBuilder().build() as MessageType
    }
}

/**
 * 用于Protobuf3的数据序列化
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-22 16:03
 */
class ProtobufSerializer<MessageType : MessageLite>(
    override val parser: Parser<MessageType>
) : BaseProtobufSerializer<MessageType>() {

    override val defaultValue: MessageType
        get() = parser.parseFrom(byteArrayOf())
}

/**
 * 用于ProtobufLite的数据序列化
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-22 16:03
 */
class ProtobufLiteSerializer<MessageType, BuilderType>(
    override val defaultValue: MessageType
) : BaseProtobufSerializer<MessageType>()
        where
MessageType : GeneratedMessageLite<MessageType, BuilderType>,
BuilderType : GeneratedMessageLite.Builder<MessageType, BuilderType> {

    constructor(
        messageType: Class<MessageType>
    ) : this(messageType.getProtobufLiteDefaultInstance())

    override val parser: Parser<MessageType> = defaultValue.parserForType
}
