package io.github.chenfei0928.content.sp.delegate

import android.content.SharedPreferences
import com.google.gson.Gson
import io.github.chenfei0928.collection.mapToIntArray
import io.github.chenfei0928.content.sp.AbsSpSaver
import io.github.chenfei0928.reflect.typeOf
import io.github.chenfei0928.repository.local.Base64Serializer
import io.github.chenfei0928.repository.local.LocalSerializer
import io.github.chenfei0928.repository.local.base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import kotlin.reflect.KProperty

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-09-03 13:38
 */
abstract class SpConvertSaver<T, R>(
    private val saver: AbsSpSaver.AbsSpDelegate<T>
) : AbsSpSaver.AbsSpDelegate0<R>() {

    override fun obtainDefaultKey(property: KProperty<*>): String {
        return saver.obtainDefaultKey(property)
    }

    override fun getValue(sp: SharedPreferences, key: String): R {
        return onRead(saver.getValue(sp, key))
    }

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: R) {
        saver.putValue(editor, key, onSave(value))
    }

    abstract fun onRead(value: T): R
    abstract fun onSave(value: R): T
}

class IntArraySpConvertSaver(
    saver: AbsSpSaver.AbsSpDelegate<String?>
) : SpConvertSaver<String?, IntArray?>(saver) {

    constructor(key: String) : this(StringDelegate(key))

    override fun onRead(value: String?): IntArray? =
        value?.split(",")
            ?.mapToIntArray { it.toIntOrNull() ?: -1 }

    override fun onSave(value: IntArray?): String? {
        return value?.joinToString(",")
    }
}

//<editor-fold defaultstate="collapsed" desc="使用Gson序列化对象">
class GsonSpConvertSaver<T>(
    saver: AbsSpSaver.AbsSpDelegate<String?>,
    private val gson: Gson = io.github.chenfei0928.util.gson.gson,
    private val type: Type
) : SpConvertSaver<String?, T?>(saver) {

    constructor(
        key: String,
        gson: Gson = io.github.chenfei0928.util.gson.gson,
        type: Type
    ) : this(StringDelegate(key), gson, type)

    override fun onRead(value: String?): T? {
        return gson.fromJson(value, type)
    }

    override fun onSave(value: T?): String? {
        return gson.toJson(value)
    }
}

inline fun <reified T> GsonSpConvertSaver(key: String) = GsonSpConvertSaver<T>(
    StringDelegate(key),
    type = typeOf<T>()
)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="使用LocalSerializer序列化对象">
class LocalSerializerSpConvertSaver<T>(
    key: String? = null,
    serializer: LocalSerializer<T>
) : AbsSpSaver.AbsSpDelegate<T?>(key) {
    private val serializer: Base64Serializer<T> = serializer.base64()

    override fun getValue(sp: SharedPreferences, key: String): T? {
        return ByteArrayInputStream(
            sp.getString(key, null)?.toByteArray()
                ?: byteArrayOf()
        ).let {
            serializer.onOpenInputStream(it)
        }.let {
            serializer.read(it)
        }
    }

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: T?) {
        if (value == null) {
            editor.remove(key)
            return
        }
        val base64Content = ByteArrayOutputStream().let { byteArrayOutputStream ->
            serializer.onOpenOutStream(byteArrayOutputStream).let {
                serializer.write(byteArrayOutputStream, value)
            }
            String(byteArrayOutputStream.toByteArray())
        }
        editor.putString(key, base64Content)
    }
}
//</editor-fold>
