package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultLazyValue
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class KtxsJsonSpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        T>
constructor(
    saver: AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, String?>,
    private val json: Json,
    private val serializer: SerializationStrategy<T>,
    private val deserializer: DeserializationStrategy<T>,
) : BaseSpConvert<SpSaver, Sp, Ed, String?, T?>(
    saver, PreferenceType.NoSupportPreferenceDataStore
) {

    constructor(
        key: String? = null,
        json: Json = Json,
        serializer: KSerializer<T>,
    ) : this(StringDelegate(key), json, serializer, serializer)

    override fun onRead(value: String): T & Any = json.decodeFromString(deserializer, value)!!
    override fun onSave(value: T & Any): String = json.encodeToString(serializer, value)

    companion object {
        inline operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified T> invoke(
            key: String? = null, json: Json = Json,
        ) = KtxsJsonSpConvert<SpSaver, Sp, Ed, T>(
            key, json, json.serializersModule.serializer<T>(),
        )

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified T> nonnull(
            key: String? = null, json: Json = Json, noinline defaultValue: () -> T & Any
        ): AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, T & Any> =
            KtxsJsonSpConvert<SpSaver, Sp, Ed, T>(
                key, json, json.serializersModule.serializer<T>(),
            ).defaultLazyValue(defaultValue)
    }
}
