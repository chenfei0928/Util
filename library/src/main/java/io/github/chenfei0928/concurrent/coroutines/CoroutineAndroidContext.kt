package io.github.chenfei0928.concurrent.coroutines

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.View
import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentViewLifecycleAccessor
import androidx.lifecycle.AndroidViewModel
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
internal class CoroutineAndroidContextImpl
private constructor(
    private val host: Any?,
    override val androidContext: Context,
    override val fragmentHost: Fragment?,
) : CoroutineAndroidContext, AbstractCoroutineContextElement(CoroutineAndroidContext) {

    override fun toString(): String {
        @Suppress("MaxLineLength")
        return "CoroutineAndroidContextImpl(host=$host, androidContext=$androidContext, fragmentHost=$fragmentHost, tagOrNull=$tagOrNull)"
    }

    override val tagOrNull: String? by lazy {
        host?.staticTag ?: fragmentHost?.staticTag ?: androidContext.staticTag
    }

    companion object {
        private val classTagMap = ArrayMap<Class<*>, String?>()

        private val Any?.staticTag: String?
            get() = this?.javaClass?.let {
                classTagMap.getOrPut(it) {
                    try {
                        it.getDeclaredField("TAG").run {
                            isAccessible = true
                            get(null) as? String
                        }
                    } catch (_: ReflectiveOperationException) {
                        null
                    }
                }
            }

        fun newInstance(host: Any?): CoroutineAndroidContext {
            return newInstanceImpl(host, host)
        }

        private fun newInstanceImpl(host: Any?, node: Any?): CoroutineAndroidContext {
            return if (node is LifecycleOwner && FragmentViewLifecycleAccessor.isInstance(node)) {
                // 使用fragment的viewLifecycle创建协程实例，通过该方式获取其fragment
                val fragment = FragmentViewLifecycleAccessor.getFragmentByViewLifecycleOwner(node)
                CoroutineAndroidContextImpl(
                    node,
                    fragment.activity ?: fragment.requireContext(),
                    fragment
                )
            } else when (node) {
                is View -> {
                    newInstanceImpl(
                        host,
                        node.findParentFragment() ?: node.context.findActivity() ?: node.context
                    )
                }
                is Dialog -> {
                    CoroutineAndroidContextImpl(node, node.context, null)
                }
                is AndroidViewModel -> {
                    CoroutineAndroidContextImpl(node, node.getApplication(), null)
                }
                is Fragment -> {
                    CoroutineAndroidContextImpl(node, node.activity ?: node.requireContext(), node)
                }
                is Activity -> {
                    CoroutineAndroidContextImpl(node, node, null)
                }
                is Context -> {
                    CoroutineAndroidContextImpl(node, node, null)
                }
                else -> {
                    CoroutineAndroidContextImpl(node, ContextProvider.context, null)
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

    /**
     * 获取当前协程作用域所绑定上下文的 TAG 字段值
     */
    val tagOrNull: String?

    override fun toString(): String

    val childFragmentManager: FragmentManager?
        get() {
            val fragment = fragmentHost
            if (fragment != null) {
                return fragment.childFragmentManager
            }
            val context = androidContext.findActivity()
            if (context is FragmentActivity) {
                return context.supportFragmentManager
            }
            return null
        }
}
