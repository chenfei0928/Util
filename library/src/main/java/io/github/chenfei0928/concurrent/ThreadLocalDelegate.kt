package io.github.chenfei0928.concurrent

import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-12-10 18:23
 */
class ThreadLocalDelegate<T>(
    private val initializer: () -> T
) : ReadWriteProperty<Any?, T> {
    private val map = ConcurrentHashMap<Thread, T>()

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return map.getOrPut(Thread.currentThread(), initializer)
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        map[Thread.currentThread()] = value
    }
}
