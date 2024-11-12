package io.github.chenfei0928.reflect

import android.util.Property
import androidx.annotation.EmptySuper
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

inline fun <T, reified V> KProperty1<T, V>.toReadOnlyProperty(): Property<T, V> {
    return KProperty1Property(this, true, V::class.java)
}

inline fun <T, reified V> KMutableProperty1<T, V>.toProperty(
    crossinline afterSetBlock: (T) -> Unit = {}
): Property<T, V> = object : KProperty1Property<T, V>(this, false, V::class.java) {
    override fun afterSetBlock(value: T) {
        afterSetBlock(value)
    }
}

open class KProperty1Property<T, V>(
    private val property: KProperty1<T, V>,
    private val isReadOnly: Boolean,
    type: Class<V>,
) : Property<T, V>(type, property.name) {
    override fun isReadOnly(): Boolean {
        return isReadOnly && property !is KMutableProperty1
    }

    override fun get(`object`: T): V {
        return property.get(`object`)
    }

    override fun set(`object`: T, value: V) {
        if (isReadOnly) {
            super.set(`object`, value)
        } else {
            (property as KMutableProperty1<T, V>).set(`object`, value)
            afterSetBlock(`object`)
        }
    }

    @EmptySuper
    open fun afterSetBlock(value: T) {
        // noop
    }
}
