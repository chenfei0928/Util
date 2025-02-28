package io.github.chenfei0928.repository.local.serializer

import io.github.chenfei0928.repository.local.LocalSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.InputStream
import java.io.OutputStream

/**
 * @author chenf()
 * @date 2024-12-17 17:43
 */
class KtxsStringSerializer<T : Any>(
    private val format: StringFormat,
    private val serializer: SerializationStrategy<T>,
    private val deserializer: DeserializationStrategy<T>,
    override val defaultValue: T
) : LocalSerializer<T> {

    constructor(
        format: StringFormat, serializer: KSerializer<T>, defaultValue: T
    ) : this(format, serializer, serializer, defaultValue)

    override fun write(outputStream: OutputStream, obj: T) {
        outputStream.writer().use {
            it.write(format.encodeToString(serializer, obj))
        }
    }

    override fun read(inputStream: InputStream): T {
        return inputStream.reader().use {
            format.decodeFromString(deserializer, it.readText())
        }
    }

    override fun copy(obj: T): T {
        return format.decodeFromString(deserializer, format.encodeToString(serializer, obj))
    }

    override fun toString(): String {
        return "KtxsStringSerializer<${serializer.descriptor.serialName}>(format=$format)"
    }

    companion object {
        inline operator fun <reified T : Any> invoke(
            defaultValue: T, json: Json = Json,
        ): LocalSerializer<T> = KtxsStringSerializer(
            json, json.serializersModule.serializer<T>(), defaultValue
        )
    }
}
