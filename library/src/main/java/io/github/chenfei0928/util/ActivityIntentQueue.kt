package io.github.chenfei0928.util

import android.content.Intent
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.annotation.ReturnThis
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import io.github.chenfei0928.app.fragment.removeSelf
import io.github.chenfei0928.lifecycle.bindUntilFirstEvent
import io.github.chenfei0928.os.safeHandler
import io.github.chenfei0928.util.ActivityIntentQueue.Task
import io.github.chenfei0928.util.ActivityIntentQueue.Task.CallbackTask.ShowFuncWithDismissListener
import java.util.Deque
import kotlin.random.Random

/**
 * 任务队列
 * activity初始化流程中可能需要连续启动多个子activity，
 * 但这些activity可能会有先后顺序，或它们之间不应覆盖，要one by one的显示
 * 1. 提供将一个[Task]添加到队列头、尾部
 * 2. 将该队列提交到[FragmentManager]中以 one-by-one 显示它们
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-01-07 15:07
 */
class ActivityIntentQueue : Fragment() {
    private val pendingTaskQueue: Deque<Task> = java.util.ArrayDeque<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postDelayedPendingTaskOrRemoveSelf()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        pendingTaskQueue.find {
            it is Task.IntentTask && it.requestCode == requestCode
        }?.let { task ->
            task as Task.IntentTask
            pendingTaskQueue.remove(task)
            task.resultCallback.invoke(resultCode, data)

            // 尝试调用下一个intent
            postDelayedPendingTaskOrRemoveSelf()
        }
    }

    internal fun postDelayedPendingTaskOrRemoveSelf() {
        // 防止在 FragmentManager 事务 FragmentTransaction 中处理fragment，在此发送message执行
        // 防止使用处保存回调后先调用回调再将回调设null，
        // 而在回调中poll下一个任务时会设置回调字段导致的回调字段设置新值后会被设null的问题
        // 回调示例字段保存的回调——>（回调中poll出下一个任务，将回调传入后将回调保存到实例的字段中）——>将实例保存的回调字段设null
        // 在此将poll任务发送到下个message去执行
        safeHandler.postDelayed(pollPendingTaskOrRemoveSelf, 1)
    }

    private val pollPendingTaskOrRemoveSelf = Runnable {
        if (!isAdded) {
            return@Runnable
        }
        val task = pendingTaskQueue.peek()
        if (task == null) {
            safeHandler.post {
                removeSelf()
            }
        } else if (task.fire(this)) {
            pendingTaskQueue.remove(task)
        }
    }

    /**
     * 添加到显示队列开始位置
     *
     * @param task 要显示的task
     */
    @ReturnThis
    fun addFirst(task: Task): ActivityIntentQueue {
        pendingTaskQueue.addFirst(task)
        return this
    }

    /**
     * 添加到显示队列最后
     *
     * @param task 要显示的task
     */
    @ReturnThis
    fun offer(task: Task): ActivityIntentQueue {
        pendingTaskQueue.offer(task)
        return this
    }

    /**
     * 将指定task移除出显示队列，如果其正在显示，不会同时自动隐藏
     *
     * @param task 要取消显示的task
     */
    fun remove(task: Task) {
        pendingTaskQueue.remove(task)
    }

    @MainThread
    fun emit(fragmentManager: FragmentManager) {
        if (isAdded || pendingTaskQueue.isEmpty()) {
            return
        }
        fragmentManager.commitNow(true) {
            add(this@ActivityIntentQueue, "ActivityIntentQueue")
        }
    }

    /**
     * 队列任务类型接口定义
     */
    sealed interface Task {
        /**
         * 使用宿主队列启动当前任务，并返回该任务类型：
         * - 如果当前任务是 Intent 型，返回 false ，以要求数组队列不要立即移除该任务，
         * 以备后续 [ActivityIntentQueue.onActivityResult] 使用和 [ActivityIntentQueue.postDelayedPendingTaskOrRemoveSelf]
         * - 如果当前方法返回后不需要额外处理，该方法内部也会在完成任务后调用
         * [ActivityIntentQueue.postDelayedPendingTaskOrRemoveSelf]，则返回 true
         *
         * @param queue 宿主任务处理器
         * @return 是否立即在任务队列中移除该任务
         */
        fun fire(queue: ActivityIntentQueue): Boolean

        /**
         * Intent 跳转到Activity并返回的任务
         *
         * @property intent 要启动Activity的 intent
         * @property resultCallback [onActivityResult]的回调
         */
        data class IntentTask(
            private val intent: Intent,
            internal val resultCallback: (resultCode: Int, data: Intent?) -> Unit,
        ) : Task {
            internal val requestCode: Int = Random.nextInt() and FRAGMENT_REQUEST_CODE_MASK

            override fun fire(queue: ActivityIntentQueue): Boolean {
                @Suppress("DEPRECATION")
                queue.startActivityForResult(intent, requestCode)
                return false
            }
        }

        /**
         * [DialogFragment] 任务，后续任务会在该 dialog [DialogFragment.dismiss] 后执行下一个
         */
        data class DialogFragmentTask(
            private val dialog: DialogFragment
        ) : Task {
            override fun fire(queue: ActivityIntentQueue): Boolean {
                if (!dialog.isAdded) {
                    dialog.showNow(queue.childFragmentManager, dialog.javaClass.simpleName)
                }
                dialog.bindUntilFirstEvent(Lifecycle.Event.ON_DESTROY) {
                    queue.postDelayedPendingTaskOrRemoveSelf()
                }
                return true
            }
        }

        /**
         * 回调型任务，后续任务会在[callback]的[ShowFuncWithDismissListener.show]的入参被调用后执行下一个
         */
        data class CallbackTask(
            private val callback: ShowFuncWithDismissListener,
        ) : Task {
            override fun fire(queue: ActivityIntentQueue): Boolean {
                callback.show {
                    queue.postDelayedPendingTaskOrRemoveSelf()
                }
                return true
            }

            /**
             * 在回调中开启显示目标任务，并在该任务完成后调用传入的回调
             */
            fun interface ShowFuncWithDismissListener {
                /**
                 * 立即显示目标任务，并在目标任务执行完毕后调用[dismissListener]，后续任务会在[dismissListener]被调用后执行
                 */
                fun show(dismissListener: () -> Unit)
            }
        }
    }

    companion object {
        private const val FRAGMENT_REQUEST_CODE_MASK = 0xFFFF
    }
}
