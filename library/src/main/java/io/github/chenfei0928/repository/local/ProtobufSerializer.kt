package io.github.chenfei0928.repository.local

import com.google.protobuf.MessageLite
import com.google.protobuf.Parser

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
