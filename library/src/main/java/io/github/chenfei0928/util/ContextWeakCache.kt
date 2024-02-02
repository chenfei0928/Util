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
open class WeakCache<T, R>(
    private val creator: () -> R
) {
    private val cache: MutableMap<T, R> = WeakHashMap()

    open operator fun get(context: T): R {
        return cache.getOrPut(context, creator)
    }
}

class ContextWeakCache<R>(
    creator: () -> R
) : WeakCache<Context, R>(creator) {

    override fun get(context: Context): R {
        return if (context is ContextWrapper && context !is Activity) {
            get(context.baseContext)
        } else {
            super.get(context)
        }
    }
}
