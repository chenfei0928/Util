package io.github.chenfei0928.concurrent

import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-12-10 18:23
 */
class ThreadLocalDelegate<V>(
    private val initializer: () -> V
) : ReadWriteProperty<Any?, V> {
    private val map = ConcurrentHashMap<Thread, V>()

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): V {
        return map.getOrPut(Thread.currentThread(), initializer)
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        map[Thread.currentThread()] = value
    }
}
