package io.github.chenfei0928.repository.datastore

import androidx.datastore.core.Serializer
import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import com.google.protobuf.protobufDefaultInstance
import com.google.protobuf.protobufParserForType
import java.io.InputStream
import java.io.OutputStream

/**
 * @author chenfei()
 * @date 2022-06-20 15:07
 */
class ProtobufSerializer<MessageType : MessageLite>(
    messageType: Class<MessageType>
) : Serializer<MessageType> {
    private val reader: Parser<MessageType> = messageType.protobufParserForType!!

    override val defaultValue: MessageType = messageType.protobufDefaultInstance!!

    override suspend fun readFrom(input: InputStream): MessageType {
        return reader.parseFrom(input)
    }

    override suspend fun writeTo(t: MessageType, output: OutputStream) {
        t.writeTo(output)
        output.flush()
    }

    companion object {
        inline operator fun <reified MessageType : MessageLite> invoke(): Serializer<MessageType> =
            ProtobufSerializer<MessageType>(MessageType::class.java)
    }
}
