package io.github.chenfei0928.repository.local.serializer

import io.github.chenfei0928.repository.local.LocalSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer
import java.io.InputStream
import java.io.OutputStream

/**
 * @author chenf()
 * @date 2024-12-17 17:43
 */
class KtxsJsonSerializer<T : Any>(
    private val json: Json,
    private val serializer: SerializationStrategy<T>,
    private val deserializer: DeserializationStrategy<T>,
    override val defaultValue: T
) : LocalSerializer<T> {

    constructor(
        json: Json, serializer: KSerializer<T>, defaultValue: T
    ) : this(json, serializer, serializer, defaultValue)

    override fun write(outputStream: OutputStream, obj: T) {
        json.encodeToStream(serializer, obj, outputStream)
    }

    override fun read(inputStream: InputStream): T {
        return json.decodeFromStream(deserializer, inputStream)
    }

    override fun copy(obj: T): T {
        return json.decodeFromJsonElement(deserializer, json.encodeToJsonElement(serializer, obj))
    }

    companion object {
        inline operator fun <reified T : Any> invoke(
            defaultValue: T, json: Json = Json,
        ): LocalSerializer<T> = KtxsJsonSerializer(
            json, json.serializersModule.serializer<T>(), defaultValue
        )
    }
}
