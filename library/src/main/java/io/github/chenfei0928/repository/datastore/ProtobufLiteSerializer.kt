package io.github.chenfei0928.repository.datastore

import androidx.datastore.core.Serializer
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.Parser
import com.google.protobuf.getProtobufLiteDefaultInstance
import com.google.protobuf.getProtobufLiteParserForType
import java.io.InputStream
import java.io.OutputStream

/**
 * @author chenfei()
 * @date 2022-06-20 15:07
 */
class ProtobufLiteSerializer<MessageType : GeneratedMessageLite<MessageType, *>>(
    messageType: Class<MessageType>
) : Serializer<MessageType> {
    private val reader: Parser<MessageType> = messageType.getProtobufLiteParserForType()

    override val defaultValue: MessageType = messageType.getProtobufLiteDefaultInstance()

    override suspend fun readFrom(input: InputStream): MessageType {
        return reader.parseFrom(input)
    }

    override suspend fun writeTo(t: MessageType, output: OutputStream) {
        t.writeTo(output)
        output.flush()
    }
}
