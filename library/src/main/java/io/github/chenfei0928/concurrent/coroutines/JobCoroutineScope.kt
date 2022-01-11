package io.github.chenfei0928.concurrent.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * 伴随着任务的协程领域，便于控制该协程
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-30 15:04
 */
open class JobCoroutineScope(
    private val constructorCoroutineContext: CoroutineContext
) : CoroutineScope {
    val job = Job() // 定义job

    override val coroutineContext: CoroutineContext
        get() = constructorCoroutineContext + job
}
