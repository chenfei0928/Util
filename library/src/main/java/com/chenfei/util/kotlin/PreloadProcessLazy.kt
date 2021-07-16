package com.chenfei.util.kotlin

import com.chenfei.util.kotlin.coroutines.UNINITIALIZED_VALUE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import okhttp3.internal.notifyAll
import okhttp3.internal.wait

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-28 15:22
 */
abstract class PreloadProcessLazy<R, T>(
    private val scope: CoroutineScope, lock: Any? = null
) : Lazy<T> {
    @Volatile
    private var _value: Any? = UNINITIALIZED_VALUE

    // final field is required to enable safe publication of constructed instance
    private val lock: Any = lock ?: this

    init {
        notifyInvalid()
    }

    private fun notifyLock() {
        synchronized(lock) {
            this@PreloadProcessLazy.lock.notifyAll()
        }
    }

    protected fun notifyInvalid() {
        _value = UNINITIALIZED_VALUE
        val async = scope.async {
            _value = initializer()
            notifyLock()
        }
        async.start()
    }

    override val value: T
        @Suppress("UNCHECKED_CAST") get() {
            val _v1 = _value
            if (_v1 !== UNINITIALIZED_VALUE) {
                preProcess(_v1 as R)?.let {
                    return it
                }
            }
            while (true) {
                synchronized(lock) {
                    try {
                        lock.wait()
                    } catch (ignore: InterruptedException) {
                    }
                }
                val _v2 = _value
                if (_v2 !== UNINITIALIZED_VALUE) {
                    preProcess(_v2 as R)?.let {
                        return it
                    }
                }
            }
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String =
        if (isInitialized()) value.toString() else "Lazy value not initialized yet."

    protected abstract suspend fun initializer(): R
    protected abstract fun preProcess(value: R): T?
}
