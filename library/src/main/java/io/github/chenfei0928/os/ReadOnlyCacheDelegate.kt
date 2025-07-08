package io.github.chenfei0928.os

import android.util.ArrayMap
import androidx.annotation.IntDef
import io.github.chenfei0928.concurrent.lazy.UNINITIALIZED_VALUE
import java.util.WeakHashMap
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-11-11 17:39
 */
open class ReadOnlyCacheDelegate<Host : Any, V>(
    private val delegate: ReadOnlyProperty<Host, V>,
    @param:CacheMode
    protected val cacheMode: Int = CACHE_MODE_DELEGATE,
) : ReadOnlyProperty<Host, V> {
    protected var caches: Any? = when (cacheMode) {
        CACHE_MODE_DELEGATE -> UNINITIALIZED_VALUE
        CACHE_MODE_INSTANCE -> WeakHashMap<Host, V>()
        CACHE_MODE_INSTANCE_WITH_PROPERTY -> WeakHashMap<Host, ArrayMap<KProperty<*>, V>>()
        else -> throw IllegalArgumentException("Invalid cache mode: $cacheMode")
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Host, property: KProperty<*>): V {
        return when (cacheMode) {
            CACHE_MODE_DELEGATE -> {
                if (caches !is UNINITIALIZED_VALUE) {
                    return caches as V
                }
                caches = delegate.getValue(thisRef, property)
                caches as V
            }
            CACHE_MODE_INSTANCE -> {
                val caches = caches as WeakHashMap<Host, V>
                caches.getOrPut(thisRef) { delegate.getValue(thisRef, property) }
            }
            CACHE_MODE_INSTANCE_WITH_PROPERTY -> {
                val caches = caches as WeakHashMap<Host, ArrayMap<KProperty<*>, V>>
                caches.getOrPut(thisRef) { ArrayMap() }
                    .getOrPut(property) { delegate.getValue(thisRef, property) }
            }
            else -> throw IllegalArgumentException("Invalid cache mode: $cacheMode")
        }
    }

    class Writable<Host : Any, V>(
        private val delegate: ReadWriteProperty<Host, V>,
        @CacheMode cacheMode: Int,
    ) : ReadOnlyCacheDelegate<Host, V>(delegate, cacheMode), ReadWriteProperty<Host, V> {
        override fun setValue(thisRef: Host, property: KProperty<*>, value: V) {
            when (cacheMode) {
                CACHE_MODE_DELEGATE -> {
                    this.caches = value
                    delegate.setValue(thisRef, property, value)
                }
                CACHE_MODE_INSTANCE -> {
                    val caches = caches as WeakHashMap<Host, V>
                    caches[thisRef] = value
                    delegate.setValue(thisRef, property, value)
                }
                CACHE_MODE_INSTANCE_WITH_PROPERTY -> {
                    val caches = caches as WeakHashMap<Host, ArrayMap<KProperty<*>, V>>
                    caches.getOrPut(thisRef) { ArrayMap() }[property] = value
                    delegate.setValue(thisRef, property, value)
                }
                else -> throw IllegalArgumentException("Invalid cache mode: $cacheMode")
            }
        }
    }

    @IntDef(
        CACHE_MODE_DELEGATE,
        CACHE_MODE_INSTANCE,
        CACHE_MODE_INSTANCE_WITH_PROPERTY
    )
    annotation class CacheMode

    companion object {
        const val CACHE_MODE_DELEGATE = 0
        const val CACHE_MODE_INSTANCE = 1
        const val CACHE_MODE_INSTANCE_WITH_PROPERTY = 2
    }
}
