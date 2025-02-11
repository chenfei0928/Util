package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete.Companion.defaultValue
import io.github.chenfei0928.content.sp.saver.delegate.ByteArrayDelegate
import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class LocalSerializerSpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        T : Any>
constructor(
    private val serializer: LocalSerializer<T>,
    saver: AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, ByteArray?>,
) : BaseSpConvert<SpSaver, Sp, Ed, ByteArray?, T?>(
    saver, PreferenceType.NoSupportPreferenceDataStore
) {

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
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                T : Any> nonnullForSp(
            serializer: LocalSerializer<T>, key: String? = null
        ): AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, T> =
            LocalSerializerSpConvert<SpSaver, Sp, Ed, T>(
                serializer, Base64StringConvert(key)
            ).defaultValue(serializer.defaultValue)

        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Sp>, Sp : MMKV, T : Any> nonnullForMmkv(
            serializer: LocalSerializer<T>, key: String? = null
        ): AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Sp, T> =
            LocalSerializerSpConvert<SpSaver, Sp, Sp, T>(
                serializer, ByteArrayDelegate<SpSaver, Sp>(key)
            ).defaultValue(serializer.defaultValue)
    }
}
