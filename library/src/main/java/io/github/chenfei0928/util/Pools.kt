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

inline fun <T : Any> Pools.Pool<T>.use(block: (T?) -> Pair<T, R>): R {
    val t = acquire()
    var tr: Pair<T, R>? = null
    return try {
        tr = block(t)
        tr.second
    } finally {
        (t ?: tr?.first)?.let {
            release(it)
        }
    }
}

inline fun <T : Any, R> Pools.Pool<T>.use(creator: () -> T, block: (T) -> R): R {
    val t = acquire() ?: creator()
    return try {
        block(t)
    } finally {
        release(t)
    }
}
