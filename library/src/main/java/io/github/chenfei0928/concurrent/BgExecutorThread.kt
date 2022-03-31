package io.github.chenfei0928.concurrent

import io.github.chenfei0928.util.Log
import java.util.concurrent.ArrayBlockingQueue

/**
 *  参考自[androidx.asynclayoutinflater.view.AsyncLayoutInflater.InflateThread]
 *  移除场景不适用的 InflateRequest 对象池
 */
internal class BgExecutorThread(
    name: String
) : Thread(name) {
    private val mQueue = ArrayBlockingQueue<Runnable>(10)

    // Extracted to its own method to ensure locals have a constrained liveness
    // scope by the GC. This is needed to avoid keeping previous request references
    // alive for an indeterminate amount of time, see b/33158143 for details
    private fun runInner() {
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
            Log.w(
                TAG, "Failed to inflate resource in the background! Retrying on the UI thread", ex
            )
        }
    }

    override fun run() {
        while (true) {
            runInner()
        }
    }

    fun contain(request: Runnable?): Boolean = mQueue.contains(request)

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

    companion object {
        private const val TAG = "KW_BgExecutorThread"
    }
}
