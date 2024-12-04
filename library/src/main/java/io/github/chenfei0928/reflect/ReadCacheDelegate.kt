package io.github.chenfei0928.reflect

import android.util.ArrayMap
import io.github.chenfei0928.collection.getContainOrPut
import io.github.chenfei0928.concurrent.lazy.UNINITIALIZED_VALUE
import java.util.WeakHashMap
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-12-03 17:50
 */
open class ReadCacheDelegate<Host, T>(
    protected open val delegate: ReadOnlyProperty<Host, T>
) : ReadOnlyProperty<Host, T> {
    protected var value: Any? = UNINITIALIZED_VALUE

    final override fun getValue(thisRef: Host, property: KProperty<*>): T {
        if (value !is UNINITIALIZED_VALUE) {
            return value as T
        }
        value = delegate.getValue(thisRef, property)
        return value as T
    }

    class Writable<Host, T>(
        override val delegate: ReadWriteProperty<Host, T>
    ) : ReadCacheDelegate<Host, T>(delegate), ReadWriteProperty<Host, T> {
        override fun setValue(thisRef: Host, property: KProperty<*>, value: T) {
            this.value = value
            delegate.setValue(thisRef, property, value)
        }
    }

    companion object {
        // Host to <PropertyName, value>
        private val cache = WeakHashMap<Any, ArrayMap<String, Any>>()

        fun <Host, T> ReadOnlyProperty<Host, T>.getCacheOrValue(
            thisRef: Host, property: KProperty<*>
        ): T = cache.getOrPut(thisRef, ::ArrayMap).getContainOrPut(property.name) {
            getValue(thisRef, property)
        } as T

        fun <Host, T> ReadWriteProperty<Host, T>.setValueAndCache(
            thisRef: Host, property: KProperty<*>, value: T
        ) {
            cache.getOrPut(thisRef, ::ArrayMap)[property.name] = value
            setValue(thisRef, property, value)
        }
    }
}
