package io.github.chenfei0928.util

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.github.chenfei0928.app.fragment.removeSelf
import io.github.chenfei0928.os.safeHandler
import java.util.LinkedList
import java.util.Queue

/**
 * 任务队列
 * activity初始化流程中可能需要连续启动多个子activity，
 * 但这些activity可能会有先后顺序，或它们之间不应覆盖，要one by one的显示
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-01-07 15:07
 */
class ActivityIntentQueue : Fragment() {
    private var emitted = false
    private val pendingTaskQueue: Queue<Any> = LinkedList()
    private val activityResultCallback: Queue<Function2<Int, Intent?, Unit>> = LinkedList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        emitted = false
        // 防止pendingTask中处理fragment，在此发送message执行
        safeHandler.post {
            pollPendingTaskOrRemoveSelf()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            // 通知回调
            activityResultCallback
                .poll()
                ?.invoke(resultCode, data)
            // 尝试调用下一个intent
            pollPendingTaskOrRemoveSelf()
        }
    }

    private fun pollPendingTaskOrRemoveSelf() {
        if (!isAdded) {
            return
        }
        when (val pendingTask = pendingTaskQueue.poll()) {
            null -> {
                safeHandler.post {
                    removeSelf()
                }
            }
            is Intent -> {
                startActivityForResult(pendingTask, REQUEST_CODE)
            }
            is ShowFuncWithDismissListener -> {
                pendingTask.show {
                    // 防止使用处保存回调后先调用回调再将回调设null，而在回调中poll下一个任务时会设置回调字段导致的回调字段设置新值后会被设null的问题
                    // 回调示例字段保存的回调——>（回调中poll出下一个任务，将回调传入后将回调保存到实例的字段中）——>将实例保存的回调字段设null
                    // 在此将poll任务发送到下个message去执行
                    safeHandler.post {
                        pollPendingTaskOrRemoveSelf()
                    }
                }
            }
            else -> {
                throw IllegalArgumentException("无法处理的请求类型: ${pendingTask.javaClass}")
            }
        }
    }

    @JvmOverloads
    fun offer(
        intent: Intent, callback: (resultCode: Int, data: Intent?) -> Unit = { _, _ -> }
    ): ActivityIntentQueue {
        pendingTaskQueue.offer(intent)
        activityResultCallback.offer(callback)
        return this
    }

    fun offer(showWithDismissListener: ShowFuncWithDismissListener): ActivityIntentQueue {
        pendingTaskQueue.offer(showWithDismissListener)
        return this
    }

    @JvmOverloads
    fun emit(fragmentManager: FragmentManager, now: Boolean = false) {
        if (isAdded || emitted || pendingTaskQueue.isEmpty()) {
            return
        }
        emitted = true
        fragmentManager
            .beginTransaction()
            .add(this, "ActivityIntentQueue")
            .run {
                if (now) {
                    commitNowAllowingStateLoss()
                } else {
                    commitAllowingStateLoss()
                }
            }
    }

    fun interface ShowFuncWithDismissListener {
        fun show(dismissListener: () -> Unit)
    }

    companion object {
        private const val REQUEST_CODE = 1
    }
}
