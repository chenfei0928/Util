package io.github.chenfei0928.os

import io.github.chenfei0928.concurrent.lazy.UNINITIALIZED_VALUE
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-11-11 17:39
 */
abstract class BaseBundleDelegate<Host : Any, T>(
    name: String? = null, defaultValue: T? = null,
) : AbsBundleProperty<Host, T>(name, defaultValue) {
    protected var value: Any? = UNINITIALIZED_VALUE

    final override fun getValue(thisRef: Host, property: KProperty<*>): T {
        if (value !is UNINITIALIZED_VALUE) {
            return value as T
        }
        value = getValueImpl(thisRef, property)
        return value as T
    }

    protected abstract fun getValueImpl(thisRef: Host, property: KProperty<*>): T
}
