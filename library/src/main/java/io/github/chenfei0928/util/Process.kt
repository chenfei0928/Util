/**
 * @author chenf()
 * @date 2024-08-01 14:50
 */
package io.github.chenfei0928.util

import io.github.chenfei0928.concurrent.coroutines.onSuspendCancellation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

inline fun <R> Process.use(block: (Process) -> R): R {
    try {
        return block(this)
    } finally {
        this.destroy()
    }
}

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
