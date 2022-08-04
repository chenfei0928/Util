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
 * sp存储转换器，用于将sp不支持的数据结构转换为sp支持的数据结构
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-09-03 13:38
 */
abstract class SpConvertSaver<SpValueType, FieldType>(
    internal val saver: AbsSpSaver.AbsSpDelegate<SpValueType>
) : AbsSpSaver.AbsSpDelegate<FieldType>() {

    override fun obtainDefaultKey(property: KProperty<*>): String {
        return saver.obtainDefaultKey(property)
    }

    override fun getValue(sp: SharedPreferences, key: String): FieldType {
        return onRead(saver.getValue(sp, key))
    }

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: FieldType) {
        saver.putValue(editor, key, onSave(value))
    }

    abstract fun onRead(value: SpValueType): FieldType
    abstract fun onSave(value: FieldType): SpValueType
}

//<editor-fold defaultstate="collapsed" desc="Java复合类型">
class IntArraySpConvertSaver(
    saver: AbsSpSaver.AbsSpDelegate0<String?>
) : SpConvertSaver<String?, IntArray?>(saver) {

    constructor(key: String) : this(StringDelegate(key))

    override fun onRead(value: String?): IntArray? =
        value?.split(",")
            ?.mapToIntArray { it.toIntOrNull() ?: -1 }

    override fun onSave(value: IntArray?): String? {
        return value?.joinToString(",")
    }
}

class EnumNameSpConvertSaver<E : Enum<E>>(
    private val enumValues: Array<E>,
    saver: AbsSpSaver.AbsSpDelegate0<String?>
) : SpConvertSaver<String?, E?>(saver) {

    constructor(
        enumValues: Array<E>, key: String? = null
    ) : this(enumValues, StringDelegate(key))

    override fun onRead(value: String?): E? {
        return enumValues.find { value == it.name }
    }

    override fun onSave(value: E?): String? {
        return value?.name
    }
}

inline fun <reified E : Enum<E>> EnumNameSpConvertSaver(key: String? = null) =
    EnumNameSpConvertSaver(enumValues<E>(), key)

class EnumSetNameSpConvertSaver<E : Enum<E>>(
    private val enumValues: Array<E>,
    saver: AbsSpSaver.AbsSpDelegate0<Set<String>?>
) : SpConvertSaver<Set<String>?, Set<E>?>(saver) {

    constructor(
        enumValues: Array<E>, key: String? = null
    ) : this(enumValues, StringSetDelegate(key))

    override fun onRead(value: Set<String>?): Set<E>? {
        return value?.mapNotNullTo(HashSet()) { item ->
            enumValues.find { enum ->
                item == enum.name
            }
        }
    }

    override fun onSave(value: Set<E>?): Set<String>? {
        return value?.mapTo(HashSet()) { it.name }
    }
}

inline fun <reified E : Enum<E>> EnumSetNameSpConvertSaver(key: String? = null) =
    EnumSetNameSpConvertSaver(enumValues<E>(), key)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="使用Gson序列化对象">
class GsonSpConvertSaver<T>(
    saver: AbsSpSaver.AbsSpDelegate0<String?>,
    private val gson: Gson = io.github.chenfei0928.util.gson.gson,
    private val type: Type
) : SpConvertSaver<String?, T?>(saver) {
    @Volatile
    private var cacheValue: Any? = this

    constructor(
        key: String,
        gson: Gson = io.github.chenfei0928.util.gson.gson,
        type: Type
    ) : this(StringDelegate(key), gson, type)

    override fun onRead(value: String?): T? {
        if (cacheValue == this) {
            synchronized(this) {
                if (cacheValue == this) {
                    cacheValue = gson.fromJson(value, type)
                }
            }
        }
        return cacheValue as T?
    }

    override fun onSave(value: T?): String? {
        cacheValue = value
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
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="空安全，添加默认值">
/**
 * 使nullable的字段委托拥有默认值的装饰器
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-12 16:39
 */
class DefaultValueSpDelete<T>(
    saver: AbsSpSaver.AbsSpDelegate<T?>,
    internal val defaultValue: T
) : SpConvertSaver<T?, T>(saver) {

    override fun onRead(value: T?): T {
        return value ?: defaultValue
    }

    override fun onSave(value: T): T? {
        return value
    }
}

fun <T> AbsSpSaver.AbsSpDelegate<T?>.defaultValue(defaultValue: T): DefaultValueSpDelete<T> =
    DefaultValueSpDelete(this, defaultValue)
//</editor-fold>
