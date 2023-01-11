package io.github.chenfei0928.concurrent.coroutines

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentViewLifecycleAccessor
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.base.ContextProvider
import io.github.chenfei0928.content.findActivity
import io.github.chenfei0928.view.findParentFragment
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * 协程领域Android上下文存储
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-03-14 17:48
 */
internal class CoroutineAndroidContextImpl(
    override val androidContext: Context, override val fragmentHost: Fragment?
) : CoroutineAndroidContext, AbstractCoroutineContextElement(CoroutineAndroidContext) {

    override fun toString(): String {
        return "CoroutineAndroidContextImpl(androidContext=$androidContext, fragmentHost=$fragmentHost)"
    }

    companion object {
        fun newInstance(host: Any?): CoroutineAndroidContext {
            return if (host is LifecycleOwner && FragmentViewLifecycleAccessor.isInstance(host)) {
                // 使用fragment的viewLifecycle创建协程实例，通过该方式获取其fragment
                val fragment = FragmentViewLifecycleAccessor.getFragmentByViewLifecycleOwner(host)
                CoroutineAndroidContextImpl(
                    fragment.activity ?: fragment.requireContext(),
                    fragment
                )
            } else when (host) {
                is View -> {
                    newInstance(
                        host.findParentFragment() ?: host.context.findActivity() ?: host.context
                    )
                }
                is Dialog -> {
                    CoroutineAndroidContextImpl(host.context, null)
                }
                is Fragment -> {
                    CoroutineAndroidContextImpl(host.activity ?: host.requireContext(), host)
                }
                is Activity -> {
                    CoroutineAndroidContextImpl(host, null)
                }
                is Context -> {
                    CoroutineAndroidContextImpl(host, null)
                }
                else -> {
                    CoroutineAndroidContextImpl(ContextProvider.context, null)
                }
            }
        }
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
