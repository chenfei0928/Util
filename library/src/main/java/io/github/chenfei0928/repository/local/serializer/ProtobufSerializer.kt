package io.github.chenfei0928.repository.local.serializer

import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import com.google.protobuf.protobufDefaultInstance
import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.InputStream
import java.io.OutputStream

/**
 * 用于Protobuf的数据序列化
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-22 16:03
 */
class ProtobufSerializer<MessageType : MessageLite>(
    private val parser: Parser<MessageType>,
    override val defaultValue: MessageType,
) : LocalSerializer<MessageType> {

    constructor(
        messageType: Class<MessageType>
    ) : this(messageType.protobufDefaultInstance)

    @Suppress("UNCHECKED_CAST")
    constructor(defaultValue: MessageType) : this(
        defaultValue.parserForType as Parser<MessageType>, defaultValue
    )

    constructor(parser: Parser<MessageType>) : this(parser, parser.parseFrom(byteArrayOf()))

    override fun write(outputStream: OutputStream, obj: MessageType) {
        obj.writeTo(outputStream)
        outputStream.flush()
    }

    override fun read(inputStream: InputStream): MessageType {
        return parser.parseFrom(inputStream)
    }

    override fun copy(obj: MessageType): MessageType {
        @Suppress("UNCHECKED_CAST")
        return obj.toBuilder().build() as MessageType
    }

    override fun toString(): String {
        return "ProtobufSerializer<${defaultValue.javaClass.name}>"
    }

    companion object {
        inline operator fun <reified T : MessageLite> invoke(): LocalSerializer<T> =
            ProtobufSerializer(T::class.java)
    }
}
