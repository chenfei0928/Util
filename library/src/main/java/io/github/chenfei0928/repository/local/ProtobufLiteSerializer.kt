package io.github.chenfei0928.repository.local

import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.Parser
import com.google.protobuf.getProtobufLiteDefaultInstance

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
