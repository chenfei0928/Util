/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-06 00:09
 */
package androidx.lifecycle

import android.content.Context
import io.github.chenfei0928.concurrent.coroutines.CoroutineAndroidContext
import io.github.chenfei0928.concurrent.coroutines.CoroutineAndroidContextImpl
import io.github.chenfei0928.concurrent.coroutines.JobCoroutineScope
import io.github.chenfei0928.concurrent.coroutines.MainScope
import io.github.chenfei0928.reflect.asType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

private const val CONTEXT_KEY = "io.github.chenfei0928.util.kotlin.CONTEXT_KEY"
private const val SCOPE_KEY = "androidx.lifecycle.UncaughtHandlerCoroutineScope.JOB_KEY"

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
        val context: Context? = getTag(CONTEXT_KEY)
            ?: this.asType<AndroidViewModel>()?.getApplication()
        return setTagIfAbsent(
            SCOPE_KEY, UncaughtHandlerCoroutineScope(context)
        )
    }

/**
 * 与生命周期绑定的协程上下文，带有异常处理
 * [博文](https://juejin.im/post/5cfb38f96fb9a07eeb139a00)
 */
private class UncaughtHandlerCoroutineScope(
    host: Any?
) : JobCoroutineScope(MainScope.coroutineContext), Closeable {
    override val coroutineContext: CoroutineContext
        get() = super.coroutineContext + // 生命周期的协程
                androidContextElement

    private val androidContextElement: CoroutineAndroidContext =
        CoroutineAndroidContextImpl.newInstance(host)

    override fun close() {
        coroutineContext.cancel()
    }
}
