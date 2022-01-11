package io.github.chenfei0928.concurrent.lazy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

/**
 * 用于协程suspend接受处理的预加载/预处理支持
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-29 11:55
 */
abstract class PreloadProcessor<R, T>(
    private val scope: CoroutineScope
) {
    private var async: Deferred<R>? = null

    @Volatile
    private var _value: Any? = UNINITIALIZED_VALUE

    init {
        notifyInvalid()
    }

    protected fun notifyInvalid() {
        _value = UNINITIALIZED_VALUE
        async = scope
            .async {
                val initializer = initializer()
                _value = initializer
                async = null
                initializer
            }
            .apply {
                start()
            }
    }

    suspend fun value(): T {
        val _v1 = _value
        if (_v1 !== UNINITIALIZED_VALUE) {
            val process = preProcess(_v1 as R)
            if (_value !== UNINITIALIZED_VALUE) {
                return process
            }
        }
        while (true) {
            async?.await()
            val _v2 = _value
            val process = preProcess(_v2 as R)
            if (_value !== UNINITIALIZED_VALUE) {
                return process
            }
        }
    }

    fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String =
        if (isInitialized()) _value.toString() else "Lazy value not initialized yet."

    protected abstract suspend fun initializer(): R
    protected abstract fun preProcess(value: R): T
}
