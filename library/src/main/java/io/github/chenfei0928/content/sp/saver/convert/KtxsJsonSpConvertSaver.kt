package io.github.chenfei0928.content.sp.saver.convert

import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class KtxsJsonSpConvertSaver<SpSaver : AbsSpSaver<SpSaver>, T>(
    saver: AbsSpSaver.AbsSpDelegateImpl<SpSaver, String?>,
    private val json: Json,
    private val serializer: SerializationStrategy<T>,
    private val deserializer: DeserializationStrategy<T>,
) : SpConvertSaver<SpSaver, String?, T?>(saver, PreferenceType.NoSupportPreferenceDataStore) {

    constructor(
        key: String? = null,
        json: Json = Json,
        serializer: KSerializer<T>,
    ) : this(StringDelegate(key), json, serializer, serializer)

    override fun onRead(value: String): T & Any = json.decodeFromString(deserializer, value)!!
    override fun onSave(value: T & Any): String = json.encodeToString(serializer, value)

    companion object {
        inline operator fun <SpSaver : AbsSpSaver<SpSaver>, reified T> invoke(
            key: String? = null, json: Json = Json,
        ) = KtxsJsonSpConvertSaver<SpSaver, T>(
            key, json, json.serializersModule.serializer<T>(),
        )
    }
}
