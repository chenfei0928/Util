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

suspend inline fun <R> Process.useSuspend(
    context: CoroutineContext = Dispatchers.IO,
    crossinline block: suspend CoroutineScope.(Process) -> R
): R {
    onSuspendCancellation {
        try {
            exitValue()
        } catch (ignore: IllegalThreadStateException) {
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
