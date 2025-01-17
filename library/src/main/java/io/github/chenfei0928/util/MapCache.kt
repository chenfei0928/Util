package io.github.chenfei0928.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import io.github.chenfei0928.collection.WeakValueMap
import io.github.chenfei0928.collection.WrapMutableMap
import io.github.chenfei0928.collection.WrapMutableMapConvertor
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

    companion object {
        fun <T, R> WeakValue(
            cache: MutableMap<T, WeakReference<R>> = WeakHashMap(),
            creator: (T) -> R,
        ) = Basic<T, R>(cache = WeakValueMap(cache), creator)

        fun <R> Application(
            cache: MutableMap<Application, R> = WeakHashMap(),
            creator: (Context) -> R
        ) = Basic<Context, R>(
            WrapMutableMap<Context, Application, R, R>(
                cache, applicationKey, WrapMutableMapConvertor.Value.NoTodo()
            ), creator
        )

        fun <R> Activity(
            cache: MutableMap<Context, R> = WeakHashMap(),
            creator: (Context) -> R
        ) = Basic<Context, R>(
            WrapMutableMap<Context, Context, R, R>(
                cache, activityKey, WrapMutableMapConvertor.Value.NoTodo()
            ), creator
        )

        private val applicationKey = object : WrapMutableMapConvertor.Key<Context, Application> {
            override fun <WKA : Context> WKA.toK(): Application =
                this.applicationContext as Application

            override fun <KA : Application> KA.toWK(): Context = this
        }

        private val activityKey = object : WrapMutableMapConvertor.Key<Context, Context> {
            override fun <WKA : Context> WKA.toK(): Context {
                var thiz: Context? = this
                while (thiz is ContextWrapper && thiz !is Activity) {
                    thiz = thiz.baseContext
                    if (thiz is Activity) {
                        return thiz
                    }
                }
                return thiz ?: this
            }

            override fun <KA : Context> KA.toWK(): Context = toK()
        }
    }
}
