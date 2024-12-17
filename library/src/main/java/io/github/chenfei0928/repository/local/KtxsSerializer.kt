package io.github.chenfei0928.repository.local

import kotlinx.serialization.KSerializer
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
class KtxsSerializer<T>(
    private val json: Json,
    private val serializer: KSerializer<T>,
    override val defaultValue: T
) : LocalSerializer<T> {

    override fun write(outputStream: OutputStream, obj: T & Any) {
        json.encodeToStream(obj, outputStream)
    }

    override fun read(inputStream: InputStream): T {
        return json.decodeFromStream(serializer, inputStream)
    }

    companion object {
        inline operator fun <reified T> invoke(
            defaultValue: T,
            json: Json = Json,
        ): LocalSerializer<T> = KtxsSerializer(
            json, json.serializersModule.serializer<T>(), defaultValue
        )
    }
}
