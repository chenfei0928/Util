package io.github.chenfei0928.concurrent.lazy

import java.util.concurrent.atomic.AtomicReference

/**
 * @author chenfei()
 * @date 2022-08-22 15:10
 */
class SuspendGetter<T>(
    initializer: suspend () -> T
) {
    private val atomicReference = AtomicReference<Any>(UNINITIALIZED_VALUE)
    private var initializer: (suspend () -> T)? = initializer

    suspend fun get(): T {
        val get = atomicReference.get()
        return if (get != UNINITIALIZED_VALUE) {
            get as T
        } else {
            val next = initializer!!.invoke()
            initializer = null
            if (atomicReference.compareAndSet(UNINITIALIZED_VALUE, next)) {
                next
            } else {
                atomicReference.get() as T
            }
        }
    }
}
