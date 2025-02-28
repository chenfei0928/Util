package io.github.chenfei0928.repository.local.serializer

import io.github.chenfei0928.repository.local.LocalSerializer
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import java.io.InputStream
import java.io.OutputStream

/**
 * @author chenf()
 * @date 2024-12-17 17:43
 */
class KtxsBinarySerializer<T : Any>(
    private val format: BinaryFormat,
    private val serializer: SerializationStrategy<T>,
    private val deserializer: DeserializationStrategy<T>,
    override val defaultValue: T
) : LocalSerializer<T> {

    constructor(
        format: BinaryFormat, serializer: KSerializer<T>, defaultValue: T
    ) : this(format, serializer, serializer, defaultValue)

    override fun write(outputStream: OutputStream, obj: T) {
        outputStream.write(format.encodeToByteArray(serializer, obj))
    }

    override fun read(inputStream: InputStream): T {
        return format.decodeFromByteArray(deserializer, inputStream.readBytes())
    }

    override fun copy(obj: T): T {
        return format.decodeFromByteArray(deserializer, format.encodeToByteArray(serializer, obj))
    }

    override fun toString(): String {
        return "KtxsBinarySerializer<${serializer.descriptor.serialName}>(format=$format)"
    }

    companion object {
        inline operator fun <reified T : Any> invoke(
            defaultValue: T,
            format: BinaryFormat = ProtoBuf,
        ): LocalSerializer<T> = KtxsBinarySerializer(
            format, format.serializersModule.serializer<T>(), defaultValue
        )
    }
}
