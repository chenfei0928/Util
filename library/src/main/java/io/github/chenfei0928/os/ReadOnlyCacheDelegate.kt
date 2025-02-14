package io.github.chenfei0928.os

import io.github.chenfei0928.concurrent.lazy.UNINITIALIZED_VALUE
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-11-11 17:39
 */
abstract class ReadOnlyCacheDelegate<Host : Any, V> : ReadOnlyProperty<Host, V> {
    protected var value: Any? = UNINITIALIZED_VALUE

    @Suppress("UNCHECKED_CAST")
    final override fun getValue(thisRef: Host, property: KProperty<*>): V {
        if (value !is UNINITIALIZED_VALUE) {
            return value as V
        }
        value = getValueImpl(thisRef, property)
        return value as V
    }

    protected abstract fun getValueImpl(thisRef: Host, property: KProperty<*>): V
}
