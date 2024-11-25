package io.github.chenfei0928.concurrent.lazy

import io.github.chenfei0928.concurrent.coroutines.IoScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-03 13:38
 */
fun <T> lazyByAutoLoad(
    scope: CoroutineScope = IoScope,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    lock: Any? = null,
    initializer: suspend () -> T
): Lazy<T> = CoroutineAutoLoadLazy(scope, start, lock, initializer)

private class CoroutineAutoLoadLazy<T>(
    scope: CoroutineScope,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    lock: Any? = null,
    initializer: suspend () -> T
) : Lazy<T> {
    // final field is required to enable safe publication of constructed instance
    private val lock: Any = lock ?: this

    init {
        val async = scope.async(start = start) {
            value = initializer.invoke()
            notifyLock()
        }
        async.start()
    }

    private fun notifyLock() {
        synchronized(lock) {
            (lock as Object).notifyAll()
        }
    }

    @field:Volatile
    override var value: T
        private field: Any? = UNINITIALIZED_VALUE
        get() {
            val _v1 = field
            if (_v1 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST") return _v1 as T
            }
            while (true) {
                synchronized(lock) {
                    try {
                        (lock as Object).wait()
                    } catch (_: InterruptedException) {
                        // noop
                    }
                }
                val _v2 = field
                if (_v2 !== UNINITIALIZED_VALUE) {
                    @Suppress("UNCHECKED_CAST") return (_v2 as T)
                }
            }
        }

    override fun isInitialized(): Boolean = value !== UNINITIALIZED_VALUE

    override fun toString(): String =
        if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}

internal object UNINITIALIZED_VALUE
