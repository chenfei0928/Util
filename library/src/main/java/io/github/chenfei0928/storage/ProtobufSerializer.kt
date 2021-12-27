/*
package io.github.chenfei0928.storage

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Parser
import java.io.InputStream
import java.io.OutputStream

/**
 * 用于Protobuf的数据序列化
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-22 16:03
 */
class ProtobufSerializer<MessageType : GeneratedMessageV3>(
    private val parser: Parser<MessageType>
) : LocalSerializer<MessageType> {

    override fun save(outputStream: OutputStream, obj: MessageType) {
        obj.writeTo(outputStream)
        outputStream.flush()
    }

    override fun load(inputStream: InputStream): MessageType {
        return parser.parseFrom(inputStream)
    }

    override fun copy(obj: MessageType): MessageType {
        return obj.toBuilder().build() as MessageType
    }
}
*/
