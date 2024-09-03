package io.github.chenfei0928.concurrent

import io.github.chenfei0928.util.StackTraceLogUtil

/**
 * 作为[java.util.concurrent.Executor]使用时将作为背景线程执行器
 */
object ExecutorUtil : BgTaskExecutor by BgTaskExecutorImpl(),
    UiTaskExecutor by UiTaskExecutorImpl(), ExecutorAndCallback {

    override fun <R> execute(commend: () -> R, callback: (R) -> Unit) {
        val throwableAction = StackTraceLogUtil.onError(1)
        postToBg {
            try {
                val r = commend()
                // 结果发送给主线程
                postToUiThread {
                    callback(r)
                }
            } catch (t: Throwable) {
                try {
                    throwableAction(t)
                } catch (ignore: Exception) {
                    // noop
                }
            }
        }
    }
}
