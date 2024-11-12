package io.github.chenfei0928.app.activity

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.util.SparseArray
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import io.github.chenfei0928.concurrent.lazy.UNINITIALIZED_VALUE
import java.io.Serializable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf

/**
 * @author chenf()
 * @date 2024-11-11 17:39
 */
abstract class BaseArgumentDelegate<Host : Any, T>(
    protected val name: String?,
    private val defaultValue: T? = null,
) : ReadOnlyProperty<Host, T> {
    protected var value: Any? = UNINITIALIZED_VALUE

    final override fun getValue(thisRef: Host, property: KProperty<*>): T {
        if (value !is UNINITIALIZED_VALUE) {
            return value as T
        }
        value = getValueImpl(thisRef, property)
        return value as T
    }

    protected abstract fun getValueImpl(thisRef: Host, property: KProperty<*>): T

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
            Boolean::class -> getBooleanExtra(name, defaultValue as? Boolean ?: false)
            BooleanArray::class -> getBooleanArrayExtra(name)
            Char::class -> getCharExtra(name, defaultValue as? Char ?: ' ')
            CharArray::class -> getCharArrayExtra(name)
            String::class -> getStringExtra(name)
            Bundle::class -> getBundleExtra(name)
            is KClass<*> -> when {
                classifier.isSubclassOf(Parcelable::class) ->
                    IntentCompat.getParcelableExtra(this, name, classifier.java)
                classifier.isSubclassOf(Serializable::class) ->
                    IntentCompat.getSerializableExtra(
                        this, name, classifier.java as Class<out Serializable>
                    )
                classifier.isSubclassOf(CharSequence::class) ->
                    getCharSequenceExtra(name)
                classifier.isSubclassOf(List::class) -> {
                    val kClass = property.returnType.arguments[0].type?.classifier as KClass<*>
                    when {
                        kClass.isSubclassOf(Parcelable::class) -> IntentCompat.getParcelableArrayListExtra(
                            this, name, kClass.java
                        )
                        kClass.isSubclassOf(CharSequence::class) ->
                            getCharSequenceArrayListExtra(name)
                        kClass == Integer::class -> getIntegerArrayListExtra(name)
                        else -> throw IllegalArgumentException("Not support return type: $property")
                    }
                }
                classifier.java.isArray -> {
                    val componentType = classifier.java.componentType
                    when {
                        Parcelable::class.java.isAssignableFrom(componentType) ->
                            IntentCompat.getParcelableArrayExtra(
                                this, name, componentType as Class<out Parcelable>
                            )
                        CharSequence::class.java.isAssignableFrom(componentType) ->
                            getCharSequenceArrayExtra(name)
                        else -> throw IllegalArgumentException("Not support return type: $property")
                    }
                }
                else -> throw IllegalArgumentException("Not support return type: $property")
            }
            else -> throw IllegalArgumentException("Not support return type: $property")
        }
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
            Boolean::class -> getBoolean(name, defaultValue as? Boolean ?: false)
            BooleanArray::class -> getBooleanArray(name)
            Char::class -> getChar(name, defaultValue as? Char ?: ' ')
            CharArray::class -> getCharArray(name)
            String::class -> getString(name)
            Bundle::class -> getBundle(name)
            is KClass<*> -> when {
                classifier.isSubclassOf(Parcelable::class) ->
                    BundleCompat.getParcelable(this, name, classifier.java)
                classifier.isSubclassOf(Serializable::class) ->
                    BundleCompat.getSerializable(
                        this, name, classifier.java as Class<out Serializable>
                    )
                classifier.isSubclassOf(CharSequence::class) ->
                    getCharSequence(name)
                classifier.isSubclassOf(List::class) -> {
                    val kClass = property.returnType.arguments[0].type?.classifier as KClass<*>
                    when {
                        kClass.isSubclassOf(Parcelable::class) -> BundleCompat.getParcelableArrayList(
                            this, name, kClass.java
                        )
                        kClass.isSubclassOf(CharSequence::class) ->
                            getCharSequenceArrayList(name)
                        kClass == Integer::class -> getIntegerArrayList(name)
                        kClass == String::class -> getStringArrayList(name)
                        else -> throw IllegalArgumentException("Not support return type: $property")
                    }
                }
                classifier.isSubclassOf(SparseArray::class) -> {
                    val kClass = property.returnType.arguments[0].type?.classifier as KClass<*>
                    BundleCompat.getSparseParcelableArray(this, name, kClass.java)
                }
                classifier.isSubclassOf(IBinder::class) -> {
                    getBinder(name)
                }
                classifier.java.isArray -> {
                    val componentType = classifier.java.componentType
                    when {
                        Parcelable::class.java.isAssignableFrom(componentType) ->
                            BundleCompat.getParcelableArray(
                                this, name, componentType as Class<out Parcelable>
                            )
                        CharSequence::class.java.isAssignableFrom(componentType) ->
                            getCharSequenceArray(name)
                        else -> throw IllegalArgumentException("Not support return type: $property")
                    }
                }
                else -> throw IllegalArgumentException("Not support return type: $property")
            }
            else -> throw IllegalArgumentException("Not support return type: $property")
        }
        return value as T
    }
}
