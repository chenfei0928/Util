package io.github.chenfei0928.repository.local

import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import com.google.protobuf.protobufDefaultInstance
import com.google.protobuf.protobufParserForType
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
    ) : this(messageType.protobufParserForType!!, messageType.protobufDefaultInstance!!)

    constructor(parser: Parser<MessageType>) : this(parser, parser.parseFrom(byteArrayOf()))

    constructor(defaultValue: MessageType) : this(
        defaultValue.parserForType as Parser<MessageType>, defaultValue
    )

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

    companion object {
        inline operator fun <reified T : MessageLite> invoke(): LocalSerializer<T> =
            ProtobufSerializer(T::class.java)
    }
}
