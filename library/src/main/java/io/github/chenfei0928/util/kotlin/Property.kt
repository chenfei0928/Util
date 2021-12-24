package io.github.chenfei0928.util.kotlin

import android.util.Property
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

fun <T, V> KProperty1<T, V>.toReadOnlyProperty(): Property<T, V> {
    return object : Property<T, V>(returnType.javaClass as Class<V>, name) {
        override fun isReadOnly(): Boolean {
            return true
        }

        override fun get(`object`: T): V {
            return this@toReadOnlyProperty.get(`object`)
        }
    }
}

fun <T, V> KMutableProperty1<T, V>.toProperty(afterSetBlock: (T) -> Unit = {}): Property<T, V> {
    return object : Property<T, V>(returnType.javaClass as Class<V>, name) {
        override fun isReadOnly(): Boolean {
            return false
        }

        override fun get(`object`: T): V {
            return this@toProperty.get(`object`)
        }

        override fun set(`object`: T, value: V) {
            this@toProperty.set(`object`, value)
            afterSetBlock(`object`)
        }
    }
}
