package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import androidx.annotation.IntRange
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.ByteArrayDelegate
import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * 使用 [LocalSerializer] 来进行序列化保存的 [BaseSpConvert]，
 * 但由于 [BaseSpConvert] 的实现受限于Kotlin语法的约束必须要返回 nullable，
 * 使用工厂方法构建实例而非构造器
 */
class LocalSerializerSpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        V : Any>
private constructor(
    private val serializer: LocalSerializer<V>,
    saver: AbsSpSaver.Delegate<SpSaver, ByteArray?>,
    override val spValueType: PreferenceType.Struct<V> = PreferenceType.Struct(serializer.defaultValue.javaClass)
) : BaseSpConvert<SpSaver, Sp, Ed, ByteArray?, V>(saver), AbsSpSaver.DefaultValue<V> {
    override val defaultValue: V = serializer.defaultValue

    override fun onRead(value: ByteArray): V {
        return ByteArrayInputStream(value).use {
            serializer.read(it)
        }
    }

    override fun onSave(value: V): ByteArray {
        return ByteArrayOutputStream().use { byteArrayOutputStream ->
            serializer.write(byteArrayOutputStream, value)
            byteArrayOutputStream.toByteArray()
        }
    }

    override fun toString(): String {
        return "LocalSerializerSpConvert(saver=$saver, serializer=$serializer)"
    }

    companion object {
        // 非空工厂方法，需要将nullable的V转换为nonnull的V
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                V : Any> invoke(
            serializer: LocalSerializer<V>,
            saver: AbsSpSaver.Delegate<SpSaver, ByteArray?>,
            spValueType: PreferenceType.Struct<V> = PreferenceType.Struct(serializer.defaultValue.javaClass)
        ): AbsSpSaver.Delegate<SpSaver, V> {
            @Suppress("UNCHECKED_CAST")
            return LocalSerializerSpConvert(
                serializer, saver, spValueType
            ) as AbsSpSaver.Delegate<SpSaver, V>
        }

        // 非空工厂方法，需要将nullable的V转换为nonnull的V
        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                reified V : Any> forSp(
            serializer: LocalSerializer<V>,
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
            spValueType: PreferenceType.Struct<V> = PreferenceType.Struct(serializer.defaultValue.javaClass)
        ): AbsSpSaver.Delegate<SpSaver, V> = invoke(
            serializer,
            Base64StringConvert(key, expireDurationInSecond),
            spValueType
        )

        // 非空工厂方法，需要将nullable的V转换为nonnull的V
        inline fun <SpSaver : AbsSpSaver<SpSaver, Sp, Sp>, Sp : MMKV, reified V : Any> forMmkv(
            serializer: LocalSerializer<V>,
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
            spValueType: PreferenceType.Struct<V> = PreferenceType.Struct(serializer.defaultValue.javaClass)
        ): AbsSpSaver.Delegate<SpSaver, V> = invoke(
            serializer,
            ByteArrayDelegate(key, expireDurationInSecond),
            spValueType
        )
    }
}
