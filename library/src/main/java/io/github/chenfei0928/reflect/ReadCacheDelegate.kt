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
open class ReadCacheDelegate<Host, V>(
    protected open val delegate: ReadOnlyProperty<Host, V>
) : ReadOnlyProperty<Host, V> {
    protected var value: Any? = UNINITIALIZED_VALUE

    @Suppress("UNCHECKED_CAST")
    final override fun getValue(thisRef: Host, property: KProperty<*>): V {
        if (value !is UNINITIALIZED_VALUE) {
            return value as V
        }
        value = delegate.getValue(thisRef, property)
        return value as V
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

        @Suppress("UNCHECKED_CAST")
        fun <Host, V> ReadOnlyProperty<Host, V>.getCacheOrValue(
            thisRef: Host, property: KProperty<*>
        ): V = cache.getOrPut(thisRef, ::ArrayMap).getContainOrPut(property.name) {
            getValue(thisRef, property)
        } as V

        fun <Host, V> ReadWriteProperty<Host, V>.setValueAndCache(
            thisRef: Host, property: KProperty<*>, value: V
        ) {
            cache.getOrPut(thisRef, ::ArrayMap)[property.name] = value
            setValue(thisRef, property, value)
        }
    }
}
