package io.github.chenfei0928.concurrent.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-03-18 10:48
 */
suspend fun <T1, T2, R> zip(
    block1: suspend CoroutineScope.() -> T1,
    block2: suspend CoroutineScope.() -> T2,
    zipBlock: (T1, T2) -> R
): R = coroutineScope {
    val result1 = async(block = block1).apply { start() }
    val result2 = async(block = block2).apply { start() }
    return@coroutineScope zipBlock(result1.await(), result2.await())
}

suspend fun <T1, T2> zip(
    block1: suspend CoroutineScope.() -> T1, block2: suspend CoroutineScope.() -> T2
): Pair<T1, T2> = zip(block1, block2, ::Pair)

/**
 * 注册一个函数，用于在[CoroutineScope]被取消时执行
 *
 * @param block
 * @receiver
 */
inline fun CoroutineScope.onCancellation(
    crossinline block: () -> Unit
) = launch {
    try {
        awaitCancellation()
    } finally {
        block()
    }
}

/**
 * 在无 [CoroutineScope] 的 suspend 函数中调用的 [onCancellation]
 *
 * @param block
 * @receiver
 */
suspend inline fun onSuspendCancellation(
    crossinline block: () -> Unit
) = coroutineScope {
    onCancellation(block)
}
