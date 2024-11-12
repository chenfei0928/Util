package io.github.chenfei0928.repository.local

import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import java.io.InputStream
import java.io.OutputStream

/**
 * 用于Protobuf的数据序列化
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-22 16:03
 */
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
