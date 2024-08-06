package io.github.chenfei0928.concurrent.coroutines

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * @author chenf()
 * @date 2023-07-30 15:14
 */
interface CoroutineStackTraceRecordContext : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<CoroutineStackTraceRecordContext>

    val stackTrace: Array<StackTraceElement>
}

internal class CoroutineStackTraceRecordContextImpl(
    private val minStackIndex: Int
) : AbstractCoroutineContextElement(CoroutineStackTraceRecordContext),
    CoroutineStackTraceRecordContext {
    override val stackTrace: Array<StackTraceElement> = Thread.currentThread().stackTrace

    override fun toString(): String {
        val stackTrace: StackTraceElement? = (minStackIndex..stackTrace.lastIndex).map {
            stackTrace[it]
        }.find { stackTraceElement ->
            stackTraceElement.fileName != null
        }
        return stackTrace.toString()
    }
}
