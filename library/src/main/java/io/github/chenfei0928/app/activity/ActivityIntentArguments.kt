/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
package io.github.chenfei0928.app.activity

import android.content.Intent
import android.os.Parcelable
import androidx.core.content.IntentCompat
import io.github.chenfei0928.app.fragment.EnumDelegate
import io.github.chenfei0928.app.fragment.ParcelableListDelegate
import kotlin.reflect.KProperty

class ParcelableDelegate<T : Parcelable>(
    private val clazz: Class<T>, name: String? = null
) : IntentDelegate<T>(name, null) {
    override fun Intent.getValueImpl(property: KProperty<*>): T {
        return IntentCompat.getParcelableExtra(
            this, name ?: property.name, clazz
        ) ?: throw IllegalArgumentException("缺少应有的字段: ${name ?: property.name}")
    }

    companion object {
        inline fun <reified T : Parcelable> ParcelableDelegate(
            name: String? = null
        ) = ParcelableDelegate(T::class.java, name)
    }
}

class NullableParcelableDelegate<T : Parcelable>(
    private val clazz: Class<T>, name: String? = null
) : IntentDelegate<T?>(name, null) {
    override fun Intent.getValueImpl(property: KProperty<*>): T? {
        return IntentCompat.getParcelableExtra(
            this, name ?: property.name, clazz
        )
    }

    companion object {
        inline fun <reified T : Parcelable> NullableParcelableDelegate(
            name: String? = null
        ) = NullableParcelableDelegate(T::class.java, name)
    }
}

class ParcelableListDelegate<T : Parcelable>(
    private val clazz: Class<T>, name: String? = null
) : IntentDelegate<List<T>>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): List<T> {
        return IntentCompat.getParcelableArrayListExtra(
            this, name ?: property.name, clazz
        ) ?: emptyList()
    }

    companion object {
        inline fun <reified T : Parcelable> ParcelableListDelegate(
            name: String? = null
        ) = ParcelableListDelegate(T::class.java, name)
    }
}

class StringDelegate(
    name: String? = null
) : IntentDelegate<String>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): String {
        return getStringExtra(name ?: property.name) ?: ""
    }
}

class StringNullableDelegate(
    name: String? = null
) : IntentDelegate<String?>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): String? {
        return getStringExtra(name ?: property.name)
    }
}

class IntDelegate(
    name: String? = null, private val defaultValue: Int = 0
) : IntentDelegate<Int>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): Int {
        return getIntExtra(name ?: property.name, defaultValue)
    }
}

class FloatDelegate(
    name: String? = null, private val defaultValue: Float = 0f
) : IntentDelegate<Float>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): Float {
        return getFloatExtra(name ?: property.name, defaultValue)
    }
}

class BooleanDelegate(
    name: String? = null
) : IntentDelegate<Boolean>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): Boolean {
        return getBooleanExtra(name ?: property.name, false)
    }
}

class EnumDelegate<E : Enum<E>>(
    private val values: Array<E>,
    private val defaultValue: E = values.first(),
    name: String? = null,
) : IntentDelegate<E>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): E {
        val name = getStringExtra(name ?: property.name)
        return values.find { it.name == name } ?: defaultValue
    }

    companion object {
        inline fun <reified E : Enum<E>> EnumDelegate(
            defaultValue: E? = null, name: String? = null
        ) = EnumDelegate(enumValues<E>(), defaultValue ?: enumValues<E>().first(), name)
    }
}
