package io.github.chenfei0928.util

import io.github.chenfei0928.util.lambdaFunction.Action1

/**
 * 作为[java.util.concurrent.Executor]使用时将作为背景线程执行器
 */
object ExecutorUtil : BgTaskExecutor by BgTaskExecutorImpl(),
    UiTaskExecutor by UiTaskExecutorImpl() {

    @JvmStatic
    fun <R> execute(commend: () -> R, callBack: Action1<R>) {
        val throwableAction = StackTraceLogUtil.onError(1)
        postToBg {
            try {
                val r = commend()
                // 结果发送给主线程
                postToUiThread {
                    callBack.call(r)
                }
            } catch (t: Throwable) {
                try {
                    throwableAction.call(t)
                } catch (ignore: Exception) {
                }
            }
        }
    }
}
