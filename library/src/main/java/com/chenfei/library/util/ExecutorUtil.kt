package com.chenfei.library.util

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import com.chenfei.library.util.lambdaFunction.Action1

object ExecutorUtil {
    private val mMainHandler = Handler(Looper.getMainLooper())
    private val bgHandler: Handler by lazy {
        // 降低后台工作线程的线程优先级
        val thread = HandlerThread("bgHandlerExecutor", 2)
        thread.start()
        return@lazy object : Handler(thread.looper) {
            override fun dispatchMessage(msg: Message) {
                try {
                    super.dispatchMessage(msg)
                } catch (ignore: Throwable) {
                }
            }
        }
    }

    @JvmStatic
    fun <R> execute(commend: () -> R, callBack: Action1<R>) {
        bgHandler.post {
            try {
                val r = commend()
                // 结果发送给主线程
                mMainHandler.post { callBack.call(r) }
            } catch (t: Throwable) {
            }
        }
    }

    @JvmStatic
    fun postToBg(r: Runnable): Boolean {
        return bgHandler.post(r)
    }

    @JvmStatic
    fun postToBgDelayed(r: Runnable, delayMillis: Long): Boolean {
        return bgHandler.postDelayed(r, delayMillis)
    }

    @JvmStatic
    fun removeBgCallbacks(r: Runnable) {
        bgHandler.removeCallbacks(r)
    }

    @JvmStatic
    fun runOnUiThread(r: Runnable): Boolean {
        return mMainHandler.post(r)
    }

    @JvmStatic
    fun runOnUiThreadDelayed(r: Runnable, delayMillis: Long): Boolean {
        return mMainHandler.postDelayed(r, delayMillis)
    }
}
