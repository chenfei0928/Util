package io.github.chenfei0928.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import java.lang.ref.WeakReference
import java.util.WeakHashMap

/**
 * 提供基于Context来获取缓存数据，在Activity的范围内保持缓存
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-06-29 14:09
 */
open class MapCache<T, R>(
    private val cache: MutableMap<T, R> = WeakHashMap(),
    private val creator: (T) -> R,
) {
    operator fun set(key: T, value: R) {
        cache[key] = value
    }

    open operator fun get(key: T): R {
        return cache.getOrPut(key) {
            creator(key)
        }
    }
}

open class MapWeakCache<T, R>(
    private val cache: MutableMap<T, WeakReference<R>> = WeakHashMap(),
    private val creator: (T) -> R,
) {
    open operator fun set(key: T, value: R) {
        cache[key] = WeakReference(value)
    }

    open operator fun get(key: T): R {
        val value = cache[key]?.get()
        return if (value == null) {
            val answer = creator(key)
            cache[key] = WeakReference(answer)
            answer
        } else {
            value
        }
    }
}

class ActivityMapCache<R>(
    cache: MutableMap<Context, R> = WeakHashMap(),
    creator: (Context) -> R
) : MapCache<Context, R>(cache, creator) {

    override fun get(key: Context): R {
        return if (key is ContextWrapper && key !is Activity) {
            get(key.baseContext)
        } else {
            super.get(key)
        }
    }
}
