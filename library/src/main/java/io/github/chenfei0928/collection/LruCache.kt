package io.github.chenfei0928.collection

import androidx.collection.LruCache

/**
 * @author chenf()
 * @date 2025-05-13 13:48
 */

inline fun <K : Any, V : Any> LruCache<K, V>.getOrPut(key: K, creator: () -> V): V {
    return this[key] ?: creator().also { put(key, it) }
}
