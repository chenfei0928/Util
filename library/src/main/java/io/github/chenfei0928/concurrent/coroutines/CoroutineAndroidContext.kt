package io.github.chenfei0928.concurrent.coroutines

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentViewLifecycleCf0928UtilAccessor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.base.UtilInitializer
import io.github.chenfei0928.content.findActivity
import io.github.chenfei0928.lang.contains
import io.github.chenfei0928.view.findParentFragment
import io.github.chenfei0928.webkit.WebViewLifecycleOwner
import java.lang.reflect.Modifier
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
    private val host: Any,
    override val androidContext: Context,
    override val fragmentHost: Fragment?,
) : CoroutineAndroidContext, AbstractCoroutineContextElement(CoroutineAndroidContext) {

    override fun toString(): String {
        @Suppress("MaxLineLength")
        return "CoroutineAndroidContextImpl(host=$host, androidContext=$androidContext, fragmentHost=$fragmentHost, tag=$tag)"
    }

    override val tag: String by lazy {
        // 过滤掉来自操作系统的 host，如 FragmentViewLifecycleOwner 或任何操作系统的 View
        val hostIfSelf = host.takeIf {
            !it.javaClass.name.startsWith("android.") && !it.javaClass.name.startsWith("androidx.")
        }
        hostIfSelf?.getStaticTag(true)
            ?: fragmentHost?.getStaticTag()
            ?: androidContext.getStaticTag()
            ?: run { hostIfSelf ?: fragmentHost ?: androidContext }.javaClass.simpleName
    }

    companion object {
        private const val TAG = "KW_CoroutineAndroidCtx"
        private val classTagMap = ArrayMap<Class<*>, String?>()

        fun Any.getStaticTag(
            tryOuterClass: Boolean = false
        ): String? = this.javaClass.let { jClass ->
            classTagMap.getOrPut(jClass) {
                if (tryOuterClass) {
                    findTagWithOuter(jClass, this)
                } else {
                    findTag(jClass, this)
                }
            }
        }

        @Suppress("ReturnCount")
        private fun findTagWithOuter(jClass: Class<*>, instance: Any?): String? {
            val tag = findTag(jClass, instance)
            if (tag != null) {
                return tag
            }
            // 如果这个类没有tag字段，向外寻找它的外部类的tag
            val declaringClass = jClass.declaringClass
            return if (declaringClass != null) {
                findTagWithOuter(declaringClass, null)
            } else {
                val fileClass = try {
                    Class.forName(jClass.name + "Kt", false, jClass.classLoader)
                } catch (_: ClassNotFoundException) {
                    return null
                }
                // 如果已经通过获取外部类到达了最外层的类，尝试获取在Kotlin文件中直接定义的字段
                val fileTag = findTag(fileClass)
                Log.w(TAG, run {
                    "findTagWithOuter: TAG 字段应定义到类内部，而非文件中：$jClass"
                })
                fileTag
            }
        }

        private fun findTag(jClass: Class<*>, instance: Any? = null): String? = try {
            jClass.getDeclaredField("TAG").run {
                isAccessible = true
                if (Modifier.STATIC in modifiers) {
                    get(null) as? String
                } else {
                    Log.w(TAG, run {
                        "findTag: TAG 字段应定义为 static，或 Kotlin 中单例类内部、普通类伴生对象中，并修饰为 const val：$this"
                    })
                    get(instance) as? String
                }
            }
        } catch (_: ReflectiveOperationException) {
            null
        }

        fun newInstance(host: Any): CoroutineAndroidContext {
            return newInstanceImpl(host, host)
        }

        private fun newInstanceImpl(host: Any, node: Any?): CoroutineAndroidContext {
            return if (node is LifecycleOwner
                && FragmentViewLifecycleCf0928UtilAccessor.isViewLifecycleOwner(node)
            ) {
                // 使用fragment的viewLifecycle创建协程实例，通过该方式获取其fragment
                val fragment = FragmentViewLifecycleCf0928UtilAccessor.getFragmentByViewLifecycleOwner(node)
                newInstanceImpl(host, fragment)
            } else when (node) {
                is View -> {
                    newInstanceImpl(
                        host,
                        node.findParentFragment() ?: node.context.findActivity() ?: node.context
                    )
                }
                is WebViewLifecycleOwner<*> -> {
                    newInstanceImpl(host, node.webView)
                }
                is Dialog -> {
                    CoroutineAndroidContextImpl(host, node.context, null)
                }
                is AndroidViewModel -> {
                    CoroutineAndroidContextImpl(host, node.getApplication(), null)
                }
                is Fragment -> {
                    CoroutineAndroidContextImpl(host, node.activity ?: node.requireContext(), node)
                }
                is Activity -> {
                    CoroutineAndroidContextImpl(host, node, null)
                }
                is Context -> {
                    CoroutineAndroidContextImpl(host, node, null)
                }
                else -> {
                    CoroutineAndroidContextImpl(host, UtilInitializer.context, null)
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
     * 获取当前协程作用域所绑定宿主的 TAG 字段值。
     * 可能会获取到 View、Fragment、Activity 的 TAG。
     * 获取时会基于反射，可能会受到混淆影响
     */
    val tag: String

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
