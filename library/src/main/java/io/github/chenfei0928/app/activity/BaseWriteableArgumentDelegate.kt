package io.github.chenfei0928.app.activity

import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.util.SparseArray
import androidx.annotation.CallSuper
import io.github.chenfei0928.collection.asArrayList
import java.io.Serializable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf

/**
 * @author chenf()
 * @date 2024-11-12 10:26
 */
abstract class BaseWriteableArgumentDelegate<Host : Any, T>(
    name: String?, defaultValue: T? = null
) : BaseArgumentDelegate<Host, T>(name, defaultValue), ReadWriteProperty<Host, T> {
    @CallSuper
    override fun setValue(thisRef: Host, property: KProperty<*>, value: T) {
        this.value = value
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
            is KClass<*> -> when {
                classifier.isSubclassOf(Parcelable::class) ->
                    putParcelable(name, value as Parcelable)
                classifier.isSubclassOf(Serializable::class) ->
                    putSerializable(name, value as Serializable)
                classifier.isSubclassOf(CharSequence::class) ->
                    putCharSequence(name, value as CharSequence)
                classifier.isSubclassOf(List::class) -> {
                    val kClass = property.returnType.arguments[0].type?.classifier as KClass<*>
                    when {
                        kClass.isSubclassOf(Parcelable::class) ->
                            putParcelableArrayList(name, (value as List<Parcelable>).asArrayList())
                        kClass.isSubclassOf(CharSequence::class) -> putCharSequenceArrayList(
                            name, (value as List<CharSequence>).asArrayList()
                        )
                        kClass == Integer::class -> putIntegerArrayList(
                            name, (value as List<Int>).asArrayList()
                        )
                        kClass == String::class -> putStringArrayList(
                            name, (value as List<String>).asArrayList()
                        )
                        else -> throw IllegalArgumentException("Not support return type: $property")
                    }
                }
                classifier.isSubclassOf(SparseArray::class) -> {
                    putSparseParcelableArray(name, value as SparseArray<Parcelable>)
                }
                classifier.isSubclassOf(IBinder::class) -> {
                    putBinder(name, value as IBinder)
                }
                classifier.java.isArray -> {
                    val componentType = classifier.java.componentType
                    when {
                        Parcelable::class.java.isAssignableFrom(componentType) ->
                            putParcelableArray(name, value as Array<Parcelable>)
                        CharSequence::class.java.isAssignableFrom(componentType) ->
                            putCharSequenceArray(name, value as Array<CharSequence>)
                        else -> throw IllegalArgumentException("Not support return type: $property")
                    }
                }
                else -> throw IllegalArgumentException("Not support return type: $property")
            }
            else -> throw IllegalArgumentException("Not support return type: $property")
        }
    }
}
