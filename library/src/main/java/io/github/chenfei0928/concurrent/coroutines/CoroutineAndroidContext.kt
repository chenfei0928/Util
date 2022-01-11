package io.github.chenfei0928.concurrent.coroutines

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * 协程领域Android上下文存储
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-03-14 17:48
 */
class CoroutineAndroidContextImpl(
    override val androidContext: Context, override val fragmentHost: Fragment?
) : CoroutineAndroidContext, AbstractCoroutineContextElement(CoroutineAndroidContext) {

    override fun toString(): String {
        return "CoroutineAndroidContextImpl(androidContext=$androidContext, fragmentHost=$fragmentHost)"
    }
}

interface CoroutineAndroidContext : CoroutineContext.Element {
    /**
     * Key for [CoroutineAndroidContext] instance in the coroutine context.
     */
    companion object Key : CoroutineContext.Key<CoroutineAndroidContext>

    /**
     * 协程发起者的Context上下文，通常是Activity的实例
     */
    val androidContext: Context

    /**
     * 宿主Fragment，如果是从Fragment发起协程
     */
    val fragmentHost: Fragment?

    override fun toString(): String
}

val CoroutineAndroidContext.childFragmentManager: FragmentManager?
    get() {
        val fragment = fragmentHost
        if (fragment != null) {
            return fragment.childFragmentManager
        }
        val context = androidContext
        if (context is FragmentActivity) {
            return context.supportFragmentManager
        }
        return null
    }
