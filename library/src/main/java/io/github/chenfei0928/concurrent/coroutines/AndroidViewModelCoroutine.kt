/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-06 00:09
 */
package io.github.chenfei0928.concurrent.coroutines

import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

private const val SCOPE_KEY = "androidx.lifecycle.UncaughtHandlerCoroutineScope.JOB_KEY"

/**
 * 提供异常处理
 * [androidx.lifecycle.viewModelScope]
 */
val AndroidViewModel.coroutineScope: CoroutineScope
    get() {
        val scope: CoroutineScope? = this.getCloseable(SCOPE_KEY)
        if (scope != null) {
            return scope + CoroutineStackTraceRecordContextImpl(4)
        }
        val coroutineScope = AndroidViewModelCoroutineScope(this)
        addCloseable(SCOPE_KEY, coroutineScope)
        return coroutineScope + CoroutineStackTraceRecordContextImpl(4)
    }

/**
 * 与生命周期绑定的协程上下文，带有异常处理
 * [博文](https://juejin.im/post/5cfb38f96fb9a07eeb139a00)
 */
private class AndroidViewModelCoroutineScope(
    host: AndroidViewModel
) : JobCoroutineScope(MainScope.coroutineContext), Closeable {
    private val androidContextElement: CoroutineAndroidContext =
        CoroutineAndroidContextImpl.newInstance(host)

    override val coroutineContext: CoroutineContext =
        super.coroutineContext + androidContextElement

    override fun close() {
        coroutineContext.cancel()
    }
}
