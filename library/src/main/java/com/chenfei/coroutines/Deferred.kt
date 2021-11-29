package com.chenfei.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-10-23 18:08
 */
suspend inline fun <T> Deferred<T>.blockCompleteOrLaunchAwait(
    scope: CoroutineScope, crossinline block: suspend (T) -> Unit
) {
    if (isCompleted) {
        block(getCompleted())
    } else {
        scope.launch {
            block(await())
        }
    }
}
