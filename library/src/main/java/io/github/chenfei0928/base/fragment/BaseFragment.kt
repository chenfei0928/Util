package io.github.chenfei0928.base.fragment

import android.os.Bundle
import android.os.Message
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.github.chenfei0928.os.safeHandler

/**
 * 基础Fragment，所有含有View的fragment必须继承于此类
 *
 * Created by Admin on 2016/3/2.
 */
abstract class BaseFragment : Fragment(), FragmentHost {
    private var mDestroyedView = false
    var isDestroyed = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isDestroyed = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDestroyedView = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDestroyedView = true
        // 移除所有信息，以让其回收
        safeHandler.removeMessages(DELAYED_TASK)
    }

    override fun onDestroy() {
        isDestroyed = true
        // 移除所有信息，以让其回收
        safeHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    protected inline fun postDelayed(delayMillis: Long, crossinline action: () -> Unit) =
        postDelayed({ action() }, delayMillis)

    protected fun postDelayed(action: Runnable, delayMillis: Long): Boolean {
        if (isDestroyed) {
            return false
        }
        val message = Message.obtain(safeHandler, action)
        message.what = DELAYED_TASK
        return safeHandler.sendMessageDelayed(message, delayMillis)
    }

    protected inline fun postDelayedWithoutView(delayMillis: Long, crossinline action: () -> Unit) =
        postDelayedWithoutView({ action() }, delayMillis)

    protected fun postDelayedWithoutView(action: Runnable, delayMillis: Long): Boolean {
        if (mDestroyedView) {
            return false
        }
        val message = Message.obtain(safeHandler, action)
        return safeHandler.sendMessageDelayed(message, delayMillis)
    }

    protected inline fun post(crossinline action: () -> Unit) = post(Runnable { action() })

    protected fun post(action: Runnable): Boolean {
        if (isDestroyed) {
            return false
        }
        val message = Message.obtain(safeHandler, action)
        message.what = DELAYED_TASK
        return safeHandler.sendMessage(message)
    }

    protected fun removeCallbacks(action: Runnable) {
        safeHandler.removeCallbacks(action)
    }

    override fun getSupportFragmentManager(): FragmentManager = childFragmentManager

    companion object {
        private const val DELAYED_TASK: Int = 0xde1a1ed
    }
}
