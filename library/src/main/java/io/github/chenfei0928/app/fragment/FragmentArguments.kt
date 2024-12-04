/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
package io.github.chenfei0928.app.fragment

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BundleCompat
import io.github.chenfei0928.collection.asArrayList
import kotlin.reflect.KProperty

class ParcelableDelegate<T : Parcelable>(
    private val clazz: Class<T>, name: String? = null
) : ArgumentDelegate<T>(name) {
    override fun Bundle.getValueImpl(property: KProperty<*>): T {
        return BundleCompat.getParcelable(
            this, name ?: property.name, clazz
        ) ?: throw IllegalArgumentException("缺少应有的字段: ${name ?: property.name}")
    }

    override fun Bundle.setValueImpl(property: KProperty<*>, value: T) {
        putParcelable(name ?: property.name, value)
    }

    companion object {
        inline fun <reified T : Parcelable> ParcelableDelegate(
            name: String? = null
        ) = ParcelableDelegate(T::class.java, name)
    }
}

class ParcelableNullableDelegate<T : Parcelable>(
    private val clazz: Class<T>, name: String? = null
) : ArgumentDelegate<T?>(name) {
    override fun Bundle.getValueImpl(property: KProperty<*>): T? {
        return BundleCompat.getParcelable(
            this, name ?: property.name, clazz
        )
    }

    override fun Bundle.setValueImpl(property: KProperty<*>, value: T?) {
        putParcelable(name ?: property.name, value)
    }

    companion object {
        inline fun <reified T : Parcelable> ParcelableNullableDelegate(
            name: String? = null
        ) = ParcelableNullableDelegate(T::class.java, name)
    }
}

class ParcelableListDelegate<T : Parcelable>(
    private val clazz: Class<T>, name: String? = null
) : ArgumentDelegate<List<T>>(name) {
    override fun Bundle.getValueImpl(property: KProperty<*>): List<T> {
        return BundleCompat.getParcelableArrayList(
            this, name ?: property.name, clazz
        ) ?: emptyList()
    }

    override fun Bundle.setValueImpl(property: KProperty<*>, value: List<T>) {
        putParcelableArrayList(name ?: property.name, value.asArrayList())
    }

    companion object {
        inline fun <reified T : Parcelable> ParcelableListDelegate(
            name: String? = null
        ) = ParcelableListDelegate(T::class.java, name)
    }
}

class StringDelegate(
    name: String? = null
) : ArgumentDelegate<String>(name) {
    override fun Bundle.getValueImpl(property: KProperty<*>): String {
        return getString(name ?: property.name, null) ?: ""
    }

    override fun Bundle.setValueImpl(property: KProperty<*>, value: String) {
        putString(name ?: property.name, value)
    }
}

class StringNullableDelegate(
    name: String? = null
) : ArgumentDelegate<String?>(name) {
    override fun Bundle.getValueImpl(property: KProperty<*>): String? {
        return getString(name ?: property.name, null)
    }

    override fun Bundle.setValueImpl(property: KProperty<*>, value: String?) {
        putString(name ?: property.name, value)
    }
}

class IntDelegate(
    name: String? = null, private val defaultValue: Int = 0
) : ArgumentDelegate<Int>(name) {
    override fun Bundle.getValueImpl(property: KProperty<*>): Int {
        return getInt(name ?: property.name, defaultValue)
    }

    override fun Bundle.setValueImpl(property: KProperty<*>, value: Int) {
        putInt(name ?: property.name, value)
    }
}

class FloatDelegate(
    name: String? = null, private val defaultValue: Float = 0f
) : ArgumentDelegate<Float>(name) {
    override fun Bundle.getValueImpl(property: KProperty<*>): Float {
        return getFloat(name ?: property.name, defaultValue)
    }

    override fun Bundle.setValueImpl(property: KProperty<*>, value: Float) {
        putFloat(name ?: property.name, value)
    }
}

class BooleanDelegate(
    name: String? = null
) : ArgumentDelegate<Boolean>(name) {
    override fun Bundle.getValueImpl(property: KProperty<*>): Boolean {
        return getBoolean(name ?: property.name)
    }

    override fun Bundle.setValueImpl(property: KProperty<*>, value: Boolean) {
        putBoolean(name ?: property.name, value)
    }
}

class EnumDelegate<E : Enum<E>>(
    private val values: Array<E>,
    private val defaultValue: E = values.first(),
    name: String? = null
) : ArgumentDelegate<E>(name) {
    override fun Bundle.getValueImpl(property: KProperty<*>): E {
        val name = getString(name ?: property.name)
        return values.find { it.name == name } ?: defaultValue
    }

    override fun Bundle.setValueImpl(property: KProperty<*>, value: E) {
        putString(name ?: property.name, value.name)
    }

    companion object {
        inline fun <reified E : Enum<E>> EnumDelegate(
            defaultValue: E? = null, name: String? = null
        ) = EnumDelegate(enumValues<E>(), defaultValue ?: enumValues<E>().first(), name)
    }
}
