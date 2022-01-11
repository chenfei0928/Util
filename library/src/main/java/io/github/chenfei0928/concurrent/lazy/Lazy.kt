package io.github.chenfei0928.concurrent.lazy

import io.github.chenfei0928.concurrent.coroutines.IoScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-03 13:38
 */
fun <T> lazyByAutoLoad(initializer: suspend () -> T): Lazy<T> = lazyByAutoLoad(IoScope, initializer)

fun <T> lazyByAutoLoad(scope: CoroutineScope, initializer: suspend () -> T): Lazy<T> {
    return CoroutineAutoLoadLazy(scope, initializer)
}

private class CoroutineAutoLoadLazy<T>(
    scope: CoroutineScope, initializer: suspend () -> T, lock: Any? = null
) : Lazy<T> {
    @Volatile
    private var _value: Any? = UNINITIALIZED_VALUE

    // final field is required to enable safe publication of constructed instance
    private val lock: Any = lock ?: this

    init {
        val async = scope.async {
            _value = initializer.invoke()
            notifyLock()
        }
        async.start()
    }

    private fun notifyLock() {
        synchronized(lock) {
            (lock as Object).notifyAll()
        }
    }

    override val value: T
        get() {
            val _v1 = _value
            if (_v1 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST") return _v1 as T
            }
            while (true) {
                synchronized(lock) {
                    try {
                        (lock as Object).wait()
                    } catch (ignore: InterruptedException) {
                    }
                }
                val _v2 = _value
                if (_v2 !== UNINITIALIZED_VALUE) {
                    @Suppress("UNCHECKED_CAST") return (_v2 as T)
                }
            }
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String =
        if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}

internal object UNINITIALIZED_VALUE
