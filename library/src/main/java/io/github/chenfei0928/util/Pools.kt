package io.github.chenfei0928.util

import androidx.core.util.Pools
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

inline fun <T : Any> Pools.Pool<T>.toNonnull(
    crossinline creator: () -> T
): NonnullPools<T> = object : NonnullPools.SimplePool<T>(this@toNonnull) {
    override fun create(): T = creator()
}

fun <T : Any> NonnullPools<T>.acquire(lifecycleOwner: LifecycleOwner): T {
    val t = acquire()
    lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            release(t)
        }
    })
    return t
}

inline fun <T : Any, R> NonnullPools<T>.use(block: (T) -> R): R {
    val t = acquire()
    return try {
        block(t)
    } finally {
        release(t)
    }
}

fun <T : Any> Pools.Pool<T>.acquire(lifecycleOwner: LifecycleOwner): T? {
    val t = acquire()
        ?: return null
    lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            release(t)
        }
    })
    return t
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

fun <T : Any> Pools.Pool<T>.acquire(lifecycleOwner: LifecycleOwner, creator: () -> T): T {
    val t = acquire() ?: creator()
    lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            release(t)
        }
    })
    return t
}

inline fun <T : Any, R> Pools.Pool<T>.use(creator: () -> T, block: (T) -> R): R {
    val t = acquire() ?: creator()
    return try {
        block(t)
    } finally {
        release(t)
    }
}
