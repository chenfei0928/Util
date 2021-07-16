package com.chenfei.util

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.MessageQueue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.chenfei.util.UiTaskExecutor.Companion.runOnUiThread
import java.util.*
import java.util.concurrent.Executor

interface BgTaskExecutor : Executor {
    fun isRunOnBgThread(): Boolean

    override fun execute(command: Runnable) {
        postToBg(command)
    }

    fun postToBg(r: Runnable): Boolean {
        return postToBg(ImmortalLifecycleOwner, Lifecycle.Event.ON_DESTROY, r)
    }

    fun postToBg(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event, r: Runnable): Boolean

    fun containInBg(r: Runnable): Boolean

    fun removeBgCallbacks(r: Runnable)
}

internal class BgTaskExecutorImpl : BgTaskExecutor {
    // 此处不要使用HandlerThread，以直接暴露其运行的Runnable调用Looper.myLooper()导致的错误
    private val bgThread = BgExecutorThread().apply {
        // 降低后台工作线程的线程优先级
        priority = 2
        start()
    }
    private val lifecycleRunnableMap = WeakHashMap<Runnable, LifecycleListenerRunnable>()

    override fun isRunOnBgThread(): Boolean = Thread.currentThread() == bgThread

    override fun postToBg(r: Runnable): Boolean {
        return bgThread.enqueue(r)
    }

    override fun postToBg(
        lifecycleOwner: LifecycleOwner, event: Lifecycle.Event, r: Runnable
    ): Boolean {
        val enqueue = bgThread.enqueue(r)
        if (enqueue) {
            object : LifecycleListenerRunnable(lifecycleOwner, event) {
                override fun unregister() {
                    removeBgCallbacks(r)
                    this.run()
                }
            }.apply {
                register()
                bgThread.enqueue(this)
                lifecycleRunnableMap[r] = this
            }
        }
        return enqueue
    }

    override fun containInBg(r: Runnable): Boolean {
        return bgThread.contain(r)
    }

    override fun removeBgCallbacks(r: Runnable) {
        bgThread.remove(r)
        val it = lifecycleRunnableMap.remove(r)
        if (it != null) {
            bgThread.remove(it)
        }
    }
}

interface UiTaskExecutor {
    fun postIdleHandlerToUiThread(idleHandler: MessageQueue.IdleHandler) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            postToUiThread {
                postRunnableToIdleHandler(idleHandler)
            }
        } else {
            postRunnableToIdleHandler(idleHandler)
        }
    }

    fun containInUi(r: Runnable): Boolean

    fun removeUiCallback(r: Runnable)

    fun postToUiThread(r: Runnable): Boolean {
        return postToUiThread(ImmortalLifecycleOwner, Lifecycle.Event.ON_DESTROY, r)
    }

    fun postToUiThread(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event, r: Runnable): Boolean

    fun runOnUiThreadDelayed(r: Runnable, delayMillis: Long): Boolean {
        return runOnUiThreadDelayed(
            ImmortalLifecycleOwner, Lifecycle.Event.ON_DESTROY, r, delayMillis
        )
    }

    fun runOnUiThreadDelayed(
        lifecycleOwner: LifecycleOwner, event: Lifecycle.Event, r: Runnable, delayMillis: Long
    ): Boolean

    private fun postRunnableToIdleHandler(idleHandler: MessageQueue.IdleHandler) {
        Looper
            .myQueue()
            .addIdleHandler(idleHandler)
    }

    companion object {

        inline fun UiTaskExecutor.runOnUiThread(crossinline r: () -> Unit): Boolean {
            return if (Looper.myLooper() != Looper.getMainLooper()) {
                postToUiThread { r() }
            } else {
                r()
                true
            }
        }
    }
}

internal class UiTaskExecutorImpl : UiTaskExecutor {
    private val mMainHandler = Handler(Looper.getMainLooper())
    private val lifecycleRunnableMap = WeakHashMap<Runnable, LifecycleListenerRunnable>()

    override fun containInUi(r: Runnable): Boolean {
        return mMainHandler.hasMessages(r.hashCode())
    }

    override fun removeUiCallback(r: Runnable) {
        mMainHandler.removeMessages(r.hashCode())
        val it = lifecycleRunnableMap.remove(r)
        if (it != null) {
            mMainHandler.removeMessages(it.hashCode())
        }
    }

    override fun postToUiThread(r: Runnable): Boolean {
        return mMainHandler.sendMessage(obtainMsg(r))
    }

    override fun postToUiThread(
        lifecycleOwner: LifecycleOwner, event: Lifecycle.Event, r: Runnable
    ): Boolean {
        val postToUiThread = postToUiThread(r)
        if (postToUiThread) {
            UiThreadLifecycleListenerRunnable(lifecycleOwner, event, r).apply {
                register()
                postToUiThread(this)
                lifecycleRunnableMap[r] = this
            }
        }
        return postToUiThread
    }

    override fun runOnUiThreadDelayed(r: Runnable, delayMillis: Long): Boolean {
        return mMainHandler.sendMessageDelayed(obtainMsg(r), delayMillis)
    }

    override fun runOnUiThreadDelayed(
        lifecycleOwner: LifecycleOwner, event: Lifecycle.Event, r: Runnable, delayMillis: Long
    ): Boolean {
        val postToUiThread = runOnUiThreadDelayed(r, delayMillis)
        if (postToUiThread) {
            UiThreadLifecycleListenerRunnable(lifecycleOwner, event, r).apply {
                register()
                runOnUiThreadDelayed(this, delayMillis)
                lifecycleRunnableMap[r] = this
            }
        }
        return postToUiThread
    }

    private fun obtainMsg(r: Runnable) = Message
        .obtain(mMainHandler, r)
        .apply {
            what = r.hashCode()
        }

    private inner class UiThreadLifecycleListenerRunnable(
        lifecycleOwner: LifecycleOwner, event: Lifecycle.Event, private val r: Runnable
    ) : LifecycleListenerRunnable(lifecycleOwner, event) {

        override fun unregister() {
            removeUiCallback(r)
            this.run()
        }
    }
}

private abstract class LifecycleListenerRunnable(
    private val lifecycleOwner: LifecycleOwner, private val event: Lifecycle.Event
) : Runnable, LifecycleEventObserver {

    final override fun run() {
        ExecutorUtil.runOnUiThread {
            this.lifecycleOwner.lifecycle.removeObserver(this)
        }
    }

    final override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (this.event == event) {
            unregister()
        }
    }

    abstract fun unregister()

    fun register() {
        ExecutorUtil.runOnUiThread {
            this.lifecycleOwner.lifecycle.addObserver(this)
        }
    }
}
