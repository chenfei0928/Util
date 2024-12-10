package io.github.chenfei0928.util

import androidx.annotation.EmptySuper
import androidx.core.util.Pools

/**
 * @author chenf()
 * @date 2023-05-24 17:37
 */
interface NonnullPools<T : Any> {
    fun acquire(): T
    fun release(instance: T)

    fun clean()

    abstract class SimplePool<T : Any>(
        private val pools: Pools.Pool<T> = Pools.SimplePool(3)
    ) : NonnullPools<T> {

        constructor(
            @androidx.annotation.IntRange(from = 1) maxPoolSize: Int
        ) : this(Pools.SimplePool(maxPoolSize))

        override fun acquire(): T {
            return pools.acquire() ?: create()
        }

        override fun release(instance: T) {
            if (!pools.release(instance)) {
                recycle(instance)
            }
        }

        override fun clean() {
            var i = pools.acquire()
            while (i != null) {
                recycle(i)
                i = pools.acquire()
            }
        }

        protected abstract fun create(): T

        @EmptySuper
        protected open fun recycle(item: T) {
            // noop
        }

        companion object {
            inline operator fun <T : Any> invoke(
                @androidx.annotation.IntRange(from = 1) maxPoolSize: Int = 3,
                crossinline creator: () -> T
            ) = object : SimplePool<T>(maxPoolSize) {
                override fun create(): T = creator()
            }
        }
    }
}
