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
        T : Any>
private constructor(
    private val serializer: LocalSerializer<T>,
    saver: AbsSpSaver.Delegate<SpSaver, ByteArray?>,
) : BaseSpConvert<SpSaver, Sp, Ed, ByteArray?, T>(
    saver, PreferenceType.NoSupportPreferenceDataStore
), AbsSpSaver.DefaultValue<T> {
    override val defaultValue: T = serializer.defaultValue

    override fun onRead(value: ByteArray): T {
        return ByteArrayInputStream(value).use {
            serializer.read(it)
        }
    }

    override fun onSave(value: T): ByteArray {
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
                T : Any> nonnullForSp(
            serializer: LocalSerializer<T>, key: String? = null
        ): AbsSpSaver.Delegate<SpSaver, T> = LocalSerializerSpConvert<SpSaver, Sp, Ed, T>(
            serializer, Base64StringConvert<SpSaver, Sp, Ed>(key)
        ) as AbsSpSaver.Delegate<SpSaver, T>

        @Suppress("UNCHECKED_CAST")
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Sp>, Sp : MMKV, T : Any> nonnullForMmkv(
            serializer: LocalSerializer<T>,
            key: String? = null,
            expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, T> = LocalSerializerSpConvert<SpSaver, Sp, Sp, T>(
            serializer, ByteArrayDelegate<SpSaver, Sp>(key, expireDurationInSecond)
        ) as AbsSpSaver.Delegate<SpSaver, T>
    }
}
