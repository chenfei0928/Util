/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-06 00:09
 */
package androidx.lifecycle

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.chenfei.base.app.BaseApplication
import com.chenfei.util.kotlin.coroutines.CoroutineAndroidContext
import com.chenfei.util.kotlin.coroutines.CoroutineAndroidContextImpl
import com.chenfei.util.kotlin.coroutines.MainScope
import kotlinx.coroutines.*
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

private const val CONTEXT_KEY = "com.chenfei.util.kotlin.CONTEXT_KEY"
private const val SCOPE_KEY = "androidx.lifecycle.UncaughtHandlerCoroutineScope.JOB_KEY"

private val ViewModel.context: Context?
    get() = getTag(CONTEXT_KEY)

fun ViewModel.setContext(context: Context) {
    setTagIfAbsent(CONTEXT_KEY, context)
}

/**
 * 提供异常处理
 * [androidx.lifecycle.viewModelScope]
 */
val ViewModel.coroutineScope: CoroutineScope
    get() {
        val scope: CoroutineScope? = this.getTag(SCOPE_KEY)
        if (scope != null) {
            return scope
        }
        return setTagIfAbsent(
            SCOPE_KEY, UncaughtHandlerCoroutineScope(context)
        )
    }

/**
 * 与生命周期绑定的协程上下文，带有异常处理
 * [博文](https://juejin.im/post/5cfb38f96fb9a07eeb139a00)
 */
private class UncaughtHandlerCoroutineScope(
    private val host: Any?
) : CoroutineScope, Closeable {
    private var job = Job() // 定义job
    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Main.immediate + MainScope.coroutineContext + job + // 生命周期的协程
                androidContextElement

    private val androidContextElement: CoroutineAndroidContext = run {
        when (host) {
            is Fragment -> {
                CoroutineAndroidContextImpl(
                    host.activity ?: host.requireContext(), host
                )
            }
            is Activity -> {
                CoroutineAndroidContextImpl(
                    host, null
                )
            }
            is Context -> {
                CoroutineAndroidContextImpl(
                    host, null
                )
            }
            else -> {
                CoroutineAndroidContextImpl(
                    BaseApplication.getInstance(), null
                )
            }
        }
    }

    override fun close() {
        coroutineContext.cancel()
    }
}
