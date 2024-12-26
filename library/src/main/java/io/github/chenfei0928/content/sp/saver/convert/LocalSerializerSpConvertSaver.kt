package io.github.chenfei0928.content.sp.saver.convert

import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate
import io.github.chenfei0928.repository.local.decorator.Base64Serializer
import io.github.chenfei0928.repository.local.decorator.Base64Serializer.Companion.base64
import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class LocalSerializerSpConvertSaver<T>(
    saver: AbsSpSaver.AbsSpDelegate0<String?>,
    serializer: LocalSerializer<T>
) : SpConvertSaver<String?, T?>(saver) {

    constructor(
        key: String,
        serializer: LocalSerializer<T>
    ) : this(StringDelegate(key), serializer)

    private val serializer: Base64Serializer<T> = serializer.base64() as Base64Serializer<T>

    override fun onRead(value: String?): T? {
        return ByteArrayInputStream(
            value?.toByteArray() ?: byteArrayOf()
        ).let {
            serializer.onOpenInputStream(it)
        }.use {
            serializer.read(it)
        }
    }

    override fun onSave(value: T?): String? {
        return if (value == null) {
            null
        } else {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                serializer.onOpenOutStream(byteArrayOutputStream).use {
                    serializer.write(byteArrayOutputStream, value)
                }
                String(byteArrayOutputStream.toByteArray())
            }
        }
    }
}
