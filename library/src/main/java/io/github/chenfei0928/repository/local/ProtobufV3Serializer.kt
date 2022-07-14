package io.github.chenfei0928.repository.local

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Parser
import java.io.InputStream
import java.io.OutputStream

/**
 * 用于Protobuf3的数据序列化
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-22 16:03
 */
class ProtobufV3Serializer<MessageType : GeneratedMessageV3>(
    private val parser: Parser<MessageType>
) : LocalSerializer<MessageType> {

    override val defaultValue: MessageType
        get() = parser.parseFrom(byteArrayOf())

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
