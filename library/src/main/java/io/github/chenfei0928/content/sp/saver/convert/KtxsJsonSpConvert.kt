package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import androidx.annotation.IntRange
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultLazyValue
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultValue
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
        V>
constructor(
    saver: AbsSpSaver.Delegate<SpSaver, String?>,
    private val json: Json,
    private val serializer: SerializationStrategy<V>,
    private val deserializer: DeserializationStrategy<V>,
) : BaseSpConvert<SpSaver, Sp, Ed, String?, V?>(
    saver, PreferenceType.NoSupportPreferenceDataStore
) {

    constructor(
        key: String? = null,
        @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        json: Json = Json,
        serializer: KSerializer<V>,
    ) : this(StringDelegate(key, expireDurationInSecond), json, serializer, serializer)

    override fun onRead(value: String): V & Any = json.decodeFromString(deserializer, value)!!
    override fun onSave(value: V & Any): String = json.encodeToString(serializer, value)

    companion object {
        inline operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified V> invoke(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
            json: Json = Json,
        ): AbsSpSaver.Delegate<SpSaver, V?> = KtxsJsonSpConvert<SpSaver, Sp, Ed, V>(
            key, expireDurationInSecond, json, json.serializersModule.serializer<V>(),
        )

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified V> nonnullByBlock(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
            json: Json = Json,
            noinline defaultValue: () -> V & Any
        ): AbsSpSaver.Delegate<SpSaver, V & Any> = KtxsJsonSpConvert<SpSaver, Sp, Ed, V>(
            key, expireDurationInSecond, json, json.serializersModule.serializer<V>(),
        ).defaultLazyValue(defaultValue)

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified V> nonnull(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
            json: Json = Json,
            defaultValue: V & Any
        ): AbsSpSaver.Delegate<SpSaver, V & Any> = KtxsJsonSpConvert<SpSaver, Sp, Ed, V>(
            key, expireDurationInSecond, json, json.serializersModule.serializer<V>(),
        ).defaultValue(defaultValue)
    }
}
