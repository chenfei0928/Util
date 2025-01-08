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
class KtxsStringSerializer<T>(
    private val format: StringFormat,
    private val serializer: SerializationStrategy<T>,
    private val deserializer: DeserializationStrategy<T>,
    override val defaultValue: T
) : LocalSerializer<T> {

    constructor(
        format: StringFormat, serializer: KSerializer<T>, defaultValue: T
    ) : this(format, serializer, serializer, defaultValue)

    override fun write(outputStream: OutputStream, obj: T & Any) {
        outputStream.write(format.encodeToString(serializer, obj).toByteArray())
    }

    override fun read(inputStream: InputStream): T {
        return format.decodeFromString(deserializer, String(inputStream.readBytes()))
    }

    companion object {
        inline operator fun <reified T> invoke(
            defaultValue: T, json: Json = Json,
        ): LocalSerializer<T> = KtxsStringSerializer(
            json, json.serializersModule.serializer<T>(), defaultValue
        )
    }
}