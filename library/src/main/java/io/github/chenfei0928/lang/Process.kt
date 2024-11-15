/**
 * @author chenf()
 * @date 2024-08-01 14:50
 */
package io.github.chenfei0928.lang

import io.github.chenfei0928.concurrent.coroutines.onSuspendCancellation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

inline fun <T, R> T.use(
    close: (T) -> Unit,
    block: (T) -> R
): R = try {
    block(this)
} finally {
    close(this)
}

inline fun <R> Process.use(
    block: (Process) -> R
): R = use(Process::destroy, block)

/**
 * 在suspend函数中使用[block]代码块处理[Process]并返回返回值
 *
 * @param R 返回值类型
 * @param context 执行[block]时要附加的协程上下文
 * @param block 要执行的协程代码块
 * @receiver 将会在该[Process]上启动
 * @return
 */
suspend inline fun <R> Process.useSuspend(
    context: CoroutineContext = Dispatchers.IO,
    crossinline block: suspend CoroutineScope.(Process) -> R
): R {
    onSuspendCancellation {
        try {
            exitValue()
        } catch (_: IllegalThreadStateException) {
            this@useSuspend.destroy()
        }
    }
    return withContext(context) {
        try {
            block(this@useSuspend)
        } finally {
            this@useSuspend.destroy()
        }
    }
}
