/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-23 15:46
 */
package io.github.chenfei0928.app.fragment

import android.content.Intent
import android.os.Parcelable
import androidx.core.content.IntentCompat
import kotlin.reflect.KProperty

class HostParcelableDelegate<T : Parcelable>(
    private val clazz: Class<T>, name: String? = null
) : HostIntentDelegate<T>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): T {
        return IntentCompat.getParcelableExtra(
            this, name ?: property.name, clazz
        ) ?: throw IllegalArgumentException("缺少应有的字段: ${name ?: property.name}")
    }

    companion object {
        inline fun <reified T : Parcelable> HostParcelableDelegate(
            name: String? = null
        ) = HostParcelableDelegate(T::class.java, name)
    }
}

class HostParcelableNullableDelegate<T : Parcelable>(
    private val clazz: Class<T>, name: String? = null
) : HostIntentDelegate<T?>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): T? {
        return IntentCompat.getParcelableExtra(
            this, name ?: property.name, clazz
        )
    }

    companion object {
        inline fun <reified T : Parcelable> HostParcelableNullableDelegate(
            name: String? = null
        ) = HostParcelableNullableDelegate(T::class.java, name)
    }
}

class HostParcelableListDelegate<T : Parcelable>(
    private val clazz: Class<T>, name: String? = null
) : HostIntentDelegate<List<T>>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): List<T> {
        return IntentCompat.getParcelableArrayListExtra(
            this, name ?: property.name, clazz
        ) ?: throw IllegalArgumentException("缺少应有的字段: ${name ?: property.name}")
    }

    companion object {
        inline fun <reified T : Parcelable> HostParcelableListDelegate(
            name: String? = null
        ) = HostParcelableListDelegate(T::class.java, name)
    }
}

class HostStringDelegate(
    name: String? = null
) : HostIntentDelegate<String>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): String {
        return getStringExtra(name ?: property.name) ?: ""
    }
}

class HostStringNullableDelegate(
    name: String? = null
) : HostIntentDelegate<String?>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): String? {
        return getStringExtra(name ?: property.name)
    }
}

class HostIntDelegate(
    name: String? = null, private val defaultValue: Int = 0
) : HostIntentDelegate<Int>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): Int {
        return getIntExtra(name ?: property.name, defaultValue)
    }
}

class HostFloatDelegate(
    name: String? = null, private val defaultValue: Float = 0f
) : HostIntentDelegate<Float>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): Float {
        return getFloatExtra(name ?: property.name, defaultValue)
    }
}

class HostBooleanDelegate(
    name: String? = null
) : HostIntentDelegate<Boolean>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): Boolean {
        return getBooleanExtra(name ?: property.name, false)
    }
}

class HostEnumDelegate<E : Enum<E>>(
    private val values: Array<E>,
    private val defaultValue: E = values.first(),
    name: String? = null
) : HostIntentDelegate<E>(name) {
    override fun Intent.getValueImpl(property: KProperty<*>): E {
        val name = getStringExtra(name ?: property.name)
        return values.find { it.name == name } ?: defaultValue
    }

    companion object {
        inline fun <reified E : Enum<E>> HostEnumDelegate(
            defaultValue: E? = null, name: String? = null
        ) = EnumDelegate(enumValues<E>(), defaultValue ?: enumValues<E>().first(), name)
    }
}
