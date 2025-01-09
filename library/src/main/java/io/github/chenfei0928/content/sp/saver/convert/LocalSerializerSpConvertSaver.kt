package io.github.chenfei0928.content.sp.saver.convert

import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate
import io.github.chenfei0928.repository.local.LocalSerializer
import io.github.chenfei0928.repository.local.decorator.Base64Serializer
import io.github.chenfei0928.repository.local.decorator.Base64Serializer.Companion.base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class LocalSerializerSpConvertSaver<T : Any>(
    serializer: LocalSerializer<T>,
    saver: AbsSpSaver.AbsSpDelegate<String?>,
) : SpConvertSaver<String?, T?>(saver, PreferenceType.NoSupportPreferenceDataStore) {

    constructor(
        serializer: LocalSerializer<T>,
        key: String? = null
    ) : this(serializer, StringDelegate(key))

    private val serializer: Base64Serializer<T> = serializer.base64() as Base64Serializer<T>

    override fun onRead(value: String): T {
        return ByteArrayInputStream(value.toByteArray()).let {
            serializer.onOpenInputStream(it)
        }.use {
            serializer.read(it)
        }
    }

    override fun onSave(value: T): String {
        return ByteArrayOutputStream().use { byteArrayOutputStream ->
            serializer.onOpenOutStream(byteArrayOutputStream).use {
                serializer.write(byteArrayOutputStream, value)
            }
            String(byteArrayOutputStream.toByteArray())
        }
    }
}
