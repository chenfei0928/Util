package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.ByteArrayDelegate
import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

open class LocalSerializerSpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        V : Any>
private constructor(
    private val serializer: LocalSerializer<V>,
    saver: AbsSpSaver.Delegate<SpSaver, ByteArray?>,
) : BaseSpConvert<SpSaver, Sp, Ed, ByteArray?, V>(
    saver, PreferenceType.NoSupportPreferenceDataStore
), AbsSpSaver.DefaultValue<V> {
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

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                V : Any> nonnullForSp(
            serializer: LocalSerializer<V>, key: String? = null
        ): AbsSpSaver.Delegate<SpSaver, V> = LocalSerializerSpConvert<SpSaver, Sp, Ed, V>(
            serializer, Base64StringConvert<SpSaver, Sp, Ed>(key)
        ) as AbsSpSaver.Delegate<SpSaver, V>

        @Suppress("UNCHECKED_CAST")
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Sp>, Sp : MMKV, V : Any> nonnullForMmkv(
            serializer: LocalSerializer<V>,
            key: String? = null,
            expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, V> = LocalSerializerSpConvert<SpSaver, Sp, Sp, V>(
            serializer, ByteArrayDelegate<SpSaver, Sp>(key, expireDurationInSecond)
        ) as AbsSpSaver.Delegate<SpSaver, V>
    }
}
