package com.chenfei.util.sp

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.chenfei.util.ExecutorUtil
import java.util.*

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-18 19:24
 */
class SpSaverAutoApply(
    spSaver: AbsSpSaver
) {
    private val spApplyTask = Runnable { spSaver.apply() }

    fun autoSave() {
        val looper = Looper.myLooper()
        if (ExecutorUtil.isRunOnBgThread()) {
            // 在背景线程执行，发送任务到背景线程以提交
            if (!ExecutorUtil.containInBg(spApplyTask)) {
                ExecutorUtil.postToBg(spApplyTask)
            }
        } else if (looper == null) {
            // 当前线程没有Looper，发送延时任务以自动提交
            if (!ExecutorUtil.containInUi(spApplyTask)) {
                ExecutorUtil.runOnUiThreadDelayed(spApplyTask, 100)
            }
        } else if (looper == Looper.getMainLooper()) {
            // 当前是主线程，发送任务到队列中以提交
            if (!ExecutorUtil.containInUi(spApplyTask)) {
                ExecutorUtil.postToUiThread(spApplyTask)
            }
        } else {
            // 有looper但不在主线程，发送到当前looper的handler执行
            val handler = getOrCreateHandler(looper)
            if (!handler.hasMessages(spApplyTask.hashCode())) {
                Message
                    .obtain(handler, spApplyTask)
                    .apply {
                        what = spApplyTask.hashCode()
                    }
                    .sendToTarget()
            }
        }
    }

    private fun containInLooper(looper: Looper, r: Runnable): Boolean {
        return if (looper == Looper.getMainLooper()) {
            ExecutorUtil.containInUi(spApplyTask)
        } else {
            getOrCreateHandler(looper).hasMessages(r.hashCode())
        }
    }

    companion object {
        private val handlerCache = WeakHashMap<Looper, Handler>()

        private fun getOrCreateHandler(looper: Looper): Handler {
            return synchronized(handlerCache) {
                handlerCache.getOrPut(looper) {
                    Handler(looper)
                }
            }
        }
    }
}
