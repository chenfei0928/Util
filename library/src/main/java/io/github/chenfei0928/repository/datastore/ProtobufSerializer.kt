package io.github.chenfei0928.repository.datastore

import androidx.datastore.core.Serializer
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.getParseFrom
import java.io.InputStream
import java.io.OutputStream

/**
 * @author chenfei()
 * @date 2022-06-20 15:07
 */
class ProtobufSerializer<MessageType : GeneratedMessageLite<MessageType, *>>(
    messageType: Class<MessageType>
) : Serializer<MessageType> {
    private val reader: (InputStream?) -> MessageType =
        messageType.getParseFrom()

    override val defaultValue: MessageType = reader(null)

    override suspend fun readFrom(input: InputStream): MessageType {
        return reader(input)
    }

    override suspend fun writeTo(t: MessageType, output: OutputStream) {
        t.writeTo(output)
        output.flush()
    }
}
