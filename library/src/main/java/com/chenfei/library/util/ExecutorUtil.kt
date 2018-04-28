package com.yikelive.util

import android.os.Handler
import android.os.Looper
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import android.util.Log
import com.yikelive.retrofitUtil.RxJavaUtil
import com.yikelive.util.lambdaFunction.Action1
import java.util.concurrent.ArrayBlockingQueue

object ExecutorUtil {
    private const val TAG = "KW_ExecutorUtil"
    private val mMainHandler = Handler(Looper.getMainLooper())
    // 此处不要使用HandlerThread，以直接暴露其运行的Runnable调用Looper.myLooper()导致的错误
    private val bgThread: BgExecutorThread by lazy {
        BgExecutorThread().apply {
            // 降低后台工作线程的线程优先级
            priority = 2
            start()
        }
    }

    @JvmStatic
    fun <R> execute(@WorkerThread commend: () -> R, @MainThread callBack: Action1<R>) {
        val throwableAction = RxJavaUtil.onError(RxJavaUtil.CELL_METHOD_STACK_TRACE + 1)
        bgThread.enqueue(Runnable {
            try {
                val r = commend()
                // 结果发送给主线程
                mMainHandler.post { callBack.call(r) }
            } catch (t: Throwable) {
                try {
                    throwableAction.accept(t)
                } catch (ignore: Exception) {
                }
            }
        })
    }

    @JvmStatic
    fun postToBg(@WorkerThread r: Runnable): Boolean {
        return bgThread.enqueue(r)
    }

    @JvmStatic
    fun removeBgCallbacks(r: Runnable) {
        bgThread.remove(r)
    }

    @JvmStatic
    fun runOnUiThread(@MainThread r: Runnable): Boolean {
        return mMainHandler.post(r)
    }

    @JvmStatic
    fun runOnUiThreadDelayed(@MainThread r: Runnable, delayMillis: Long): Boolean {
        return mMainHandler.postDelayed(r, delayMillis)
    }

    /**
     *  参考自[android.support.v4.view.AsyncLayoutInflater.InflateThread]
     *  移除场景不适用的 InflateRequest 对象池
     */
    private class BgExecutorThread : Thread("bgThreadExecutor") {
        private val mQueue = ArrayBlockingQueue<Runnable>(10)

        // Extracted to its own method to ensure locals have a constrained liveness
        // scope by the GC. This is needed to avoid keeping previous request references
        // alive for an indeterminate amount of time, see b/33158143 for details
        fun runInner() {
            val request: Runnable
            try {
                request = mQueue.take()
            } catch (ex: InterruptedException) {
                // Odd, just continue
                Log.w(TAG, ex)
                return
            }

            try {
                request.run()
            } catch (ex: RuntimeException) {
                // Probably a Looper failure, retry on the UI thread
                Log.w(TAG, "Failed to inflate resource in the background! Retrying on the UI" + " thread", ex)
            }
        }

        override fun run() {
            while (true) {
                runInner()
            }
        }

        fun enqueue(request: Runnable): Boolean {
            return try {
                mQueue.put(request)
                true
            } catch (e: InterruptedException) {
                Log.e(TAG, "Failed to enqueue async inflate request", e)
                false
            }
        }

        fun remove(task: Runnable): Boolean {
            return mQueue.remove(task)
        }
    }
}
