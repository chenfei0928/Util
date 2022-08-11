package io.github.chenfei0928.io

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * 使用协程上下文启动一个阻塞代码块，并在协程上下文cancel时关闭对象
 */
suspend fun <T : AutoCloseable> T.launchBlocking(
    context: CoroutineContext = Dispatchers.IO,
    block: CoroutineScope.(T) -> Unit
) {
    val coroutineScope = CoroutineScope(currentCoroutineContext())
    coroutineScope.launch(context) {
        suspendCancellableCoroutine<Unit> {
            coroutineScope.launch(context) {
                block(this@launchBlocking)
            }
            it.invokeOnCancellation {
                this@launchBlocking.close()
            }
        }
    }
}
