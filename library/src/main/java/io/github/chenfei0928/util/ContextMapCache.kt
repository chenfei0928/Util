package io.github.chenfei0928.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
    operator fun set(context: T, value: R) {
        cache[context] = value
    }

    open operator fun get(context: T): R {
        return cache.getOrPut(context) {
            creator(context)
        }
    }
}

class ContextMapCache<R>(
    cache: MutableMap<Context, R> = WeakHashMap(),
    creator: (Context) -> R
) : MapCache<Context, R>(cache, creator) {

    override fun get(context: Context): R {
        return if (context is ContextWrapper && context !is Activity) {
            get(context.baseContext)
        } else {
            super.get(context)
        }
    }
}
