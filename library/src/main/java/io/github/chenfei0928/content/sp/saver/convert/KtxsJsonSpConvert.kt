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
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

class KtxsJsonSpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        V : Any> : BaseSpConvert<SpSaver, Sp, Ed, String?, V?> {
    private val json: Json
    private val serializer: SerializationStrategy<V>
    private val deserializer: DeserializationStrategy<V>
    override val spValueType: PreferenceType.Struct<V?>

    constructor(
        saver: AbsSpSaver.Delegate<SpSaver, String?>,
        json: Json = Json,
        serializer: SerializationStrategy<V>,
        deserializer: DeserializationStrategy<V>,
        spValueType: PreferenceType.Struct<V?>,
    ) : super(saver) {
        this.json = json
        this.serializer = serializer
        this.deserializer = deserializer
        this.spValueType = spValueType
    }

    constructor(
        serializer: KSerializer<V>,
        spValueType: PreferenceType.Struct<V?>,
        key: String? = null,
        @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        json: Json = Json,
    ) : super(StringDelegate(key, expireDurationInSecond)) {
        this.json = json
        this.serializer = serializer
        this.deserializer = serializer
        this.spValueType = spValueType
    }

    constructor(
        kType: KType,
        key: String? = null,
        @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        json: Json = Json,
    ) : super(StringDelegate(key, expireDurationInSecond)) {
        @Suppress("UNCHECKED_CAST") val serializer =
            json.serializersModule.serializer(kType) as KSerializer<V>
        this.json = json
        this.serializer = serializer
        this.deserializer = serializer
        this.spValueType = PreferenceType.Struct<V?>(kType.javaType)
    }

    constructor(
        type: Type,
        key: String? = null,
        @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        json: Json = Json,
    ) : super(StringDelegate(key, expireDurationInSecond)) {
        @Suppress("UNCHECKED_CAST") val serializer =
            json.serializersModule.serializer(type) as KSerializer<V>
        this.json = json
        this.serializer = serializer
        this.deserializer = serializer
        this.spValueType = PreferenceType.Struct<V?>(type)
    }

    override fun onRead(value: String): V = json.decodeFromString(deserializer, value)
    override fun onSave(value: V): String = json.encodeToString(serializer, value)
    override fun toString(): String = "KtxsJsonSpConvert(saver=$saver, spValueType=$spValueType)"

    /**
     * 提供 inline 获取 [V] 类型的工厂构造器，但以下构造器中的 `json.serializersModule.serializer<V>()` 会有一些时间消耗。
     * 建议使用带 `KSerializer<V>` 的构造器
     *
     * @constructor Create empty Companion
     */
    companion object {
        inline operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified V : Any> invoke(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
            json: Json = Json,
        ): AbsSpSaver.Delegate<SpSaver, V?> = KtxsJsonSpConvert<SpSaver, Sp, Ed, V>(
            key = key,
            expireDurationInSecond = expireDurationInSecond,
            json = json,
            serializer = json.serializersModule.serializer<V>(),
            spValueType = PreferenceType.Struct<V?>()
        )

        inline operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified V : Any> invoke(
            serializer: KSerializer<V>,
            type: Type? = null,
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
            json: Json = Json,
        ): AbsSpSaver.Delegate<SpSaver, V?> = KtxsJsonSpConvert<SpSaver, Sp, Ed, V>(
            key = key,
            expireDurationInSecond = expireDurationInSecond,
            json = json,
            serializer = serializer,
            spValueType = if (type != null) PreferenceType.Struct<V?>(type) else PreferenceType.Struct<V?>()
        )

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified V : Any> nonnullByBlock(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
            json: Json = Json,
            noinline defaultValue: () -> V
        ): AbsSpSaver.Delegate<SpSaver, V> = KtxsJsonSpConvert<SpSaver, Sp, Ed, V>(
            key = key,
            expireDurationInSecond = expireDurationInSecond,
            json = json,
            serializer = json.serializersModule.serializer<V>(),
            spValueType = PreferenceType.Struct<V?>()
        ).defaultLazyValue(defaultValue)

        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified V : Any> nonnull(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
            json: Json = Json,
            defaultValue: V
        ): AbsSpSaver.Delegate<SpSaver, V> = KtxsJsonSpConvert<SpSaver, Sp, Ed, V>(
            key = key,
            expireDurationInSecond = expireDurationInSecond,
            json = json,
            serializer = json.serializersModule.serializer<V>(),
            spValueType = PreferenceType.Struct<V?>()
        ).defaultValue(defaultValue)
    }
}
