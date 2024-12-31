package io.github.chenfei0928.util

import android.content.Context
import android.content.ContextWrapper
import java.lang.ref.WeakReference
import java.util.WeakHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 提供基于Context来获取缓存数据，在Activity的范围内保持缓存
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-06-29 14:09
 */
interface MapCache<T, R> : ReadWriteProperty<T, R> {
    operator fun set(key: T, value: R)

    operator fun get(key: T): R

    override fun setValue(thisRef: T, property: KProperty<*>, value: R) {
        set(thisRef, value)
    }

    override fun getValue(thisRef: T, property: KProperty<*>): R {
        return get(thisRef)
    }

    open class Basic<T, R>(
        private val cache: MutableMap<T, R> = WeakHashMap(),
        private val creator: (T) -> R,
    ) : MapCache<T, R> {
        override operator fun set(key: T, value: R) {
            cache[key] = value
        }

        override operator fun get(key: T): R {
            return cache.getOrPut(key) {
                creator(key)
            }
        }
    }

    open class Weak<T, R>(
        private val cache: MutableMap<T, WeakReference<R>> = WeakHashMap(),
        private val creator: (T) -> R,
    ) : MapCache<T, R> {
        override operator fun set(key: T, value: R) {
            cache[key] = WeakReference(value)
        }

        override operator fun get(key: T): R {
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

    class Activity<R>(
        cache: MutableMap<Context, R> = WeakHashMap(),
        creator: (Context) -> R
    ) : Basic<Context, R>(cache, creator) {
        override fun set(key: Context, value: R) {
            if (key is ContextWrapper && key !is android.app.Activity) {
                set(key.baseContext, value)
            } else {
                super.set(key, value)
            }
            super.set(key, value)
        }

        override fun get(key: Context): R {
            return if (key is ContextWrapper && key !is android.app.Activity) {
                get(key.baseContext)
            } else {
                super.get(key)
            }
        }
    }

    class Application<R>(
        cache: MutableMap<Context, R> = WeakHashMap(),
        creator: (Context) -> R
    ) : Basic<Context, R>(cache, creator) {
        override fun set(key: Context, value: R) {
            super.set(key.applicationContext, value)
        }

        override fun get(key: Context): R {
            return super.get(key.applicationContext)
        }
    }
}
