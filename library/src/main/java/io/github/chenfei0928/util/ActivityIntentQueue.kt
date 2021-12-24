package io.github.chenfei0928.util

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.github.chenfei0928.util.kotlin.removeSelf
import java.util.*

/**
 * activity初始化流程中可能需要连续启动多个子activity，
 * 但这些activity可能会有先后顺序，或它们之间不应覆盖，要one by one的显示
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-01-07 15:07
 */
class ActivityIntentQueue : Fragment() {
    private val queue: Queue<Any> = LinkedList()
    private val activityResultCallback: Queue<Function2<Int, Intent?, Unit>> = LinkedList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        pollIntentOrRemoveSelf()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            // 通知回调
            activityResultCallback
                .poll()
                ?.invoke(resultCode, data)
            // 尝试调用下一个intent
            pollIntentOrRemoveSelf()
        }
    }

    private fun pollIntentOrRemoveSelf() {
        when (val intent = queue.poll()) {
            null -> {
                removeSelf()
            }
            is Intent -> {
                startActivityForResult(intent, REQUEST_CODE)
            }
            is ShowFuncWithDismissListener -> {
                intent.show {
                    if (isAdded) {
                        pollIntentOrRemoveSelf()
                    }
                }
            }
            else -> {
                throw IllegalArgumentException("无法处理的请求类型: ${intent.javaClass}")
            }
        }
    }

    @JvmOverloads
    fun offer(
        intent: Intent, callback: (resultCode: Int, data: Intent?) -> Unit = { _, _ -> }
    ): ActivityIntentQueue {
        queue.offer(intent)
        activityResultCallback.offer(callback)
        return this
    }

    fun offer(showWithDismissListener: ShowFuncWithDismissListener): ActivityIntentQueue {
        queue.offer(showWithDismissListener)
        return this
    }

    fun emit(fragmentManager: FragmentManager) {
        if (isAdded || queue.isEmpty()) {
            return
        }
        fragmentManager
            .beginTransaction()
            .add(this, "ActivityIntentQueue")
            .commitAllowingStateLoss()
    }

    interface ShowFuncWithDismissListener {
        fun show(dismissListener: () -> Unit)
    }

    companion object {
        private const val REQUEST_CODE = 1
    }
}
