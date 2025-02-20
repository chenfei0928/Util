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
) : BaseSpConvert<SpSaver, Sp, Ed, ByteArray?, V>(saver), AbsSpSaver.DefaultValue<V> {
    override val spValueType = PreferenceType.Struct<V>(serializer.defaultValue.javaClass)
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
        @Suppress("UNCHECKED_CAST")
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                V : Any> nonnullForSp(
            serializer: LocalSerializer<V>,
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, V> = LocalSerializerSpConvert<SpSaver, Sp, Ed, V>(
            serializer, Base64StringConvert<SpSaver, Sp, Ed>(key, expireDurationInSecond)
        ) as AbsSpSaver.Delegate<SpSaver, V>

        // 非空工厂方法，需要将nullable的V转换为nonnull的V
        @Suppress("UNCHECKED_CAST")
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Sp>, Sp : MMKV, V : Any> nonnullForMmkv(
            serializer: LocalSerializer<V>,
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, V> = LocalSerializerSpConvert<SpSaver, Sp, Sp, V>(
            serializer, ByteArrayDelegate<SpSaver, Sp>(key, expireDurationInSecond)
        ) as AbsSpSaver.Delegate<SpSaver, V>
    }
}
