package io.github.chenfei0928.util

import androidx.core.util.Pools

inline fun <T : Any> Pools.Pool<T>.toNonnull(
    crossinline creator: () -> T
): NonnullPools<T> = object : NonnullPools.SimplePool<T>(this@toNonnull) {
    override fun create(): T = creator()
}

inline fun <T : Any, R> NonnullPools<T>.use(block: (T) -> R): R {
    val t = acquire()
    return try {
        block(t)
    } finally {
        release(t)
    }
}
