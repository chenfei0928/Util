package io.github.chenfei0928.concurrent.coroutines

import io.github.chenfei0928.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

abstract class AbsCoroutineExceptionHandler : CoroutineExceptionHandler,
    AbstractCoroutineContextElement(CoroutineExceptionHandler.Key)

/**
 * 通用的协程异常处理器，用于补充[GlobalScope]不带错误处理，在协程中出异常直接导致应用崩溃的问题
 * [博文](https://www.jianshu.com/p/2056d5424001)
 */
object UncaughtLogCoroutineExceptionHandler : AbsCoroutineExceptionHandler() {
    private const val TAG = "KW_CoroutineExceptionH"
    var onErrorLis: ((context: CoroutineContext, exception: Throwable) -> Unit)? = null

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        Log.w(TAG, buildString {
            append("UncaughtCoroutineExceptionHandler: ")
            append(context[CoroutineAndroidContext])
            appendLine()
            append("launch by: ")
            append(context[CoroutineStackTraceRecordContext])
        }, exception)
        onErrorLis?.invoke(context, exception)
    }
}

/**
 * 用于IO操作的协程领域，带有异常处理
 */
object IoScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Dispatchers.IO + UncaughtLogCoroutineExceptionHandler
}

/**
 * 默认的协程领域，带有异常处理
 */
object DefaultScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Dispatchers.Default + UncaughtLogCoroutineExceptionHandler
}

/**
 * 用于主线程操作的协程领域，带有异常处理
 */
object MainScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Dispatchers.Main.immediate + UncaughtLogCoroutineExceptionHandler
}
