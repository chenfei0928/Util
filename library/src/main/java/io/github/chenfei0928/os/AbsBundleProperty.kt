package io.github.chenfei0928.os

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.util.Size
import android.util.SizeF
import android.util.SparseArray
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.getProtobufLiteDefaultInstance
import com.google.protobuf.getProtobufV3DefaultInstance
import io.github.chenfei0928.collection.asArrayList
import io.github.chenfei0928.util.DependencyChecker
import java.io.Serializable
import kotlin.jvm.java
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

/**
 * @author chenf()
 * @date 2024-12-03 14:19
 */
@Suppress(
    "UNCHECKED_CAST",
    "CyclomaticComplexMethod",
    "LongMethod",
    "NestedBlockDepth",
    "kotlin:S3776",
)
abstract class AbsBundleProperty<Host, T>(
    protected val name: String? = null,
    private val defaultValue: T? = null,
) : ReadOnlyProperty<Host, T> {

    protected fun Intent.getTExtra(property: KProperty<*>): T {
        val name = name ?: property.name
        val value = when (val classifier = property.returnType.classifier) {
            Byte::class -> getByteExtra(name, defaultValue as? Byte ?: 0)
            ByteArray::class -> getByteArrayExtra(name)
            Short::class -> getShortExtra(name, defaultValue as? Short ?: 0)
            ShortArray::class -> getShortArrayExtra(name)
            Int::class -> getIntExtra(name, defaultValue as? Int ?: 0)
            IntArray::class -> getIntArrayExtra(name)
            Long::class -> getLongExtra(name, defaultValue as? Long ?: 0)
            LongArray::class -> getLongArrayExtra(name)
            Float::class -> getFloatExtra(name, defaultValue as? Float ?: 0f)
            FloatArray::class -> getFloatArrayExtra(name)
            Double::class -> getDoubleExtra(name, defaultValue as? Double ?: 0.0)
            DoubleArray::class -> getDoubleArrayExtra(name)
            Boolean::class -> getBooleanExtra(name, defaultValue as? Boolean == true)
            BooleanArray::class -> getBooleanArrayExtra(name)
            Char::class -> getCharExtra(name, defaultValue as? Char ?: ' ')
            CharArray::class -> getCharArrayExtra(name)
            String::class -> getStringExtra(name)
            Bundle::class -> getBundleExtra(name)
            // Intent 不支持Size与SizeF
            is KClass<*> -> when {
                classifier.isSubclassOf(Parcelable::class) ->
                    IntentCompat.getParcelableExtra(this, name, classifier.java)
                classifier.isSubclassOf(CharSequence::class) ->
                    getCharSequenceExtra(name)
                classifier.isSubclassOf(List::class) -> {
                    val kClass = property.returnType.argument0TypeClass
                    when {
                        kClass.isSubclassOf(Parcelable::class) ->
                            IntentCompat.getParcelableArrayListExtra(this, name, kClass.java)
                        kClass == String::class -> getStringArrayListExtra(name)
                        kClass.isSubclassOf(CharSequence::class) ->
                            getCharSequenceArrayListExtra(name)
                        kClass == Integer::class -> getIntegerArrayListExtra(name)
                        else -> property.throwNotSupportType()
                    }
                }
                // Intent 不支持SparseArray与IBinder
                classifier.isSubclassOf(Enum::class) -> {
                    val clazz = classifier.java as Class<out Enum<*>>
                    val name = getStringExtra(property.name)
                    java.lang.Enum.valueOf(clazz, name!!)
                }
                classifier.java.isArray -> {
                    val componentType = classifier.java.componentType!!
                    when {
                        Parcelable::class.java.isAssignableFrom(componentType) ->
                            IntentCompat.getParcelableArrayExtra(
                                this, name, componentType as Class<out Parcelable>
                            )
                        String::class.java == componentType ->
                            getStringArrayExtra(name)
                        CharSequence::class.java.isAssignableFrom(componentType) ->
                            getCharSequenceArrayExtra(name)
                        else -> property.throwNotSupportType()
                    }
                }
                classifier.isSubclassOf(Serializable::class) -> IntentCompat.getSerializableExtra(
                    this, name, classifier.java as Class<out Serializable>
                )
                DependencyChecker.PROTOBUF_LITE() && classifier.isSubclassOf(GeneratedMessageLite::class) -> {
                    val data = getByteArrayExtra(name)
                    if (data == null) {
                        null
                    } else {
                        val clazz =
                            classifier.java as Class<out GeneratedMessageLite<*, *>>
                        if (clazz.isAssignableFrom(GeneratedMessageV3::class.java)) {
                            (clazz as Class<out GeneratedMessageV3>).getProtobufV3DefaultInstance()
                        } else {
                            clazz.getProtobufLiteDefaultInstance()
                        }.parserForType.parseFrom(data)
                    }
                }
                else -> property.throwNotSupportType()
            }
            else -> property.throwNotSupportType()
        } ?: defaultValue
        return value as T
    }

    protected fun Bundle.getT(property: KProperty<*>): T {
        val name = name ?: property.name
        val value = when (val classifier = property.returnType.classifier) {
            Byte::class -> getByte(name, defaultValue as? Byte ?: 0)
            ByteArray::class -> getByteArray(name)
            Short::class -> getShort(name, defaultValue as? Short ?: 0)
            ShortArray::class -> getShortArray(name)
            Int::class -> getInt(name, defaultValue as? Int ?: 0)
            IntArray::class -> getIntArray(name)
            Long::class -> getLong(name, defaultValue as? Long ?: 0)
            LongArray::class -> getLongArray(name)
            Float::class -> getFloat(name, defaultValue as? Float ?: 0f)
            FloatArray::class -> getFloatArray(name)
            Double::class -> getDouble(name, defaultValue as? Double ?: 0.0)
            DoubleArray::class -> getDoubleArray(name)
            Boolean::class -> getBoolean(name, defaultValue as? Boolean == true)
            BooleanArray::class -> getBooleanArray(name)
            Char::class -> getChar(name, defaultValue as? Char ?: ' ')
            CharArray::class -> getCharArray(name)
            String::class -> getString(name)
            Bundle::class -> getBundle(name)
            Size::class -> getSize(name)
            SizeF::class -> getSizeF(name)
            is KClass<*> -> when {
                classifier.isSubclassOf(Parcelable::class) ->
                    BundleCompat.getParcelable(this, name, classifier.java)
                classifier.isSubclassOf(CharSequence::class) ->
                    getCharSequence(name)
                classifier.isSubclassOf(List::class) -> {
                    val kClass = property.returnType.argument0TypeClass
                    when {
                        kClass.isSubclassOf(Parcelable::class) ->
                            BundleCompat.getParcelableArrayList(this, name, kClass.java)
                        kClass == String::class -> getStringArrayList(name)
                        kClass.isSubclassOf(CharSequence::class) ->
                            getCharSequenceArrayList(name)
                        kClass == Integer::class -> getIntegerArrayList(name)
                        else -> property.throwNotSupportType()
                    }
                }
                classifier.isSubclassOf(SparseArray::class) -> {
                    val kClass = property.returnType.argument0TypeClass
                    BundleCompat.getSparseParcelableArray(this, name, kClass.java)
                }
                classifier.isSubclassOf(IBinder::class) -> getBinder(name)
                classifier.isSubclassOf(Enum::class) -> {
                    val clazz = classifier.java as Class<out Enum<*>>
                    val name = getString(property.name)
                    java.lang.Enum.valueOf(clazz, name!!)
                }
                classifier.java.isArray -> {
                    val componentType = classifier.java.componentType!!
                    when {
                        Parcelable::class.java.isAssignableFrom(componentType) ->
                            BundleCompat.getParcelableArray(
                                this, name, componentType as Class<out Parcelable>
                            )
                        String::class.java == componentType -> getStringArray(name)
                        CharSequence::class.java.isAssignableFrom(componentType) ->
                            getCharSequenceArray(name)
                        else -> property.throwNotSupportType()
                    }
                }
                classifier.isSubclassOf(Serializable::class) -> BundleCompat.getSerializable(
                    this, name, classifier.java as Class<out Serializable>
                )
                DependencyChecker.PROTOBUF_LITE() && classifier.isSubclassOf(GeneratedMessageLite::class) -> {
                    val data = getByteArray(name)
                    if (data == null) {
                        null
                    } else {
                        val clazz =
                            classifier.java as Class<out GeneratedMessageLite<*, *>>
                        if (clazz.isAssignableFrom(GeneratedMessageV3::class.java)) {
                            (clazz as Class<out GeneratedMessageV3>).getProtobufV3DefaultInstance()
                        } else {
                            clazz.getProtobufLiteDefaultInstance()
                        }.parserForType.parseFrom(data)
                    }
                }
                else -> property.throwNotSupportType()
            }
            else -> property.throwNotSupportType()
        } ?: defaultValue
        return value as T
    }

    protected fun <T> Bundle.putT(property: KProperty<*>, value: T) {
        val name = name ?: property.name
        when (val classifier = property.returnType.classifier) {
            Byte::class -> putByte(name, value as Byte)
            ByteArray::class -> putByteArray(name, value as ByteArray)
            Short::class -> putShort(name, value as Short)
            ShortArray::class -> putShortArray(name, value as ShortArray)
            Int::class -> putInt(name, value as Int)
            IntArray::class -> putIntArray(name, value as IntArray)
            Long::class -> putLong(name, value as Long)
            LongArray::class -> putLongArray(name, value as LongArray)
            Float::class -> putFloat(name, value as Float)
            FloatArray::class -> putFloatArray(name, value as FloatArray)
            Double::class -> putDouble(name, value as Double)
            DoubleArray::class -> putDoubleArray(name, value as DoubleArray)
            Boolean::class -> putBoolean(name, value as Boolean)
            BooleanArray::class -> putBooleanArray(name, value as BooleanArray)
            Char::class -> putChar(name, value as Char)
            CharArray::class -> putCharArray(name, value as CharArray)
            String::class -> putString(name, value as String)
            Bundle::class -> putBundle(name, value as Bundle)
            Size::class -> putSize(name, value as Size)
            SizeF::class -> putSizeF(name, value as SizeF)
            is KClass<*> -> when {
                classifier.isSubclassOf(Parcelable::class) -> putParcelable(
                    name, value as Parcelable
                )
                classifier.isSubclassOf(CharSequence::class) -> putCharSequence(
                    name, value as CharSequence
                )
                classifier.isSubclassOf(List::class) -> {
                    val kClass = property.returnType.argument0TypeClass
                    when {
                        kClass.isSubclassOf(Parcelable::class) ->
                            putParcelableArrayList(name, (value as List<Parcelable>).asArrayList())
                        kClass == String::class -> putStringArrayList(
                            name, (value as List<String>).asArrayList()
                        )
                        kClass.isSubclassOf(CharSequence::class) -> putCharSequenceArrayList(
                            name, (value as List<CharSequence>).asArrayList()
                        )
                        kClass == Integer::class -> putIntegerArrayList(
                            name, (value as List<Int>).asArrayList()
                        )
                        else -> property.throwNotSupportType()
                    }
                }
                classifier.isSubclassOf(SparseArray::class) -> putSparseParcelableArray(
                    name, value as SparseArray<out Parcelable>
                )
                classifier.isSubclassOf(IBinder::class) -> putBinder(name, value as IBinder)
                classifier.isSubclassOf(Enum::class) -> putString(name, (value as Enum<*>).name)
                classifier.java.isArray -> {
                    val componentType = classifier.java.componentType!!
                    when {
                        Parcelable::class.java.isAssignableFrom(componentType) ->
                            putParcelableArray(name, value as Array<out Parcelable>)
                        String::class.java == componentType ->
                            putStringArray(name, value as Array<String>)
                        CharSequence::class.java.isAssignableFrom(componentType) ->
                            putCharSequenceArray(name, value as Array<out CharSequence>)
                        else -> property.throwNotSupportType()
                    }
                }
                classifier.isSubclassOf(Serializable::class) -> putSerializable(
                    name, value as Serializable
                )
                DependencyChecker.PROTOBUF_LITE() && classifier.isSubclassOf(GeneratedMessageLite::class) -> {
                    putByteArray(name, (value as? GeneratedMessageLite<*, *>)?.toByteArray())
                }
                else -> property.throwNotSupportType()
            }
            else -> property.throwNotSupportType()
        }
    }

    private fun KProperty<*>.throwNotSupportType() {
        throw IllegalArgumentException("Not support return type: $this")
    }

    private val KType.argument0TypeClass: KClass<*>
        get() = arguments[0].type?.classifier as KClass<*>
}
