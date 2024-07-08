package io.github.chenfei0928.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import io.github.chenfei0928.base.ContextProvider
import io.github.chenfei0928.base.ContextProvider.Companion.context
import io.github.chenfei0928.util.Log
import java.lang.ref.WeakReference

/**
 * Toast工具帮助类，缓存View，但不复用Toast
 *
 * @author Admin
 * @date 2015/8/28
 */
object ToastUtil {
    private const val TAG = "KW_ToastUtil"
    private val sHandler = Handler(Looper.getMainLooper())
    private val sToastFactory: ToastFactory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ToastFactoryImplQ()
    } else {
        ToastFactoryImpl()
    }

    /**
     * Toast暂存，下次使用时取消上一个Toast，防止应用退出后Toast像吃了哔一样往外弹
     */
    @Volatile
    private var sToast: WeakReference<Toast>? = null

    @Volatile
    private var sToastShowTask: ToastShowTask? = null

    @Deprecated("使用携带 Context/Fragment 参的 showShort")
    fun showShort(@StringRes message: Int) {
        Log.w(TAG, "showShort: 无Context参 Toast", Exception())
        val context = context
        showShort(context, context.getString(message))
    }

    @Deprecated("使用携带 Context/Fragment 参的 showShort")
    fun showShort(message: String?) {
        Log.w(TAG, "showShort: 无Context参 Toast", Exception())
        showShort(context, message)
    }

    fun showShort(context: Fragment, @StringRes message: Int) {
        showShort(context.context, message)
    }

    fun showShort(context: Fragment, message: String?) {
        showShort(context.context, message)
    }

    fun showShort(context: Context?, @StringRes message: Int) {
        if (context == null) {
            Log.w(TAG, "showShort: context is null", NullPointerException())
            showShort(ContextProvider.context, message)
            return
        }
        showShort(context, context.getString(message))
    }

    /**
     * 将正在展示的toast取消并toast新的message
     * 如果消息为空，将不会显示它，也不会取消正在显示的toast
     */
    fun showShort(context: Context?, message: String?) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        if (context == null) {
            Log.w(TAG, "showShort: ", NullPointerException("context is null"))
            showShort(ContextProvider.context, message)
            return
        }
        cancel()
        if (Looper.myLooper() != Looper.getMainLooper()) {
            sToastShowTask = ToastShowTask(context, message).apply {
                sHandler.post(this)
            }
        } else {
            val toast = sToastFactory.makeText(context, message, Toast.LENGTH_SHORT)
            sToast = WeakReference(toast)
            toast.show()
        }
    }

    fun cancel() {
        sToastShowTask?.let {
            sHandler.removeCallbacks(it)
        }
        sToastShowTask = null
        sToast?.get()?.cancel()
        sToast = null
    }

    private class ToastShowTask(
        private val context: Context,
        private val msg: String?
    ) : Runnable {

        override fun run() {
            showShort(context, msg)
            sToastShowTask = null
        }
    }

    //<editor-fold defaultstate="collapsing" desc="Toast工厂">
    private sealed interface ToastFactory {
        fun makeText(context: Context, text: CharSequence?, duration: Int): Toast
    }

    private class ToastFactoryImpl : ToastFactory {
        private var sToastView: View? = null

        override fun makeText(context: Context, text: CharSequence?, duration: Int): Toast {
            // 11以下缓存toastView，以提升toast创建的性能
            // 10 以上添加了 LayoutInflater#tryInflatePrecompiled 来优化layoutInflater性能，但10中并没有启用
            val toast = Toast(context)
            toast.view = getToastView(context)
            toast.setText(text)
            return toast
        }

        @SuppressLint("ShowToast")
        private fun getToastView(context: Context): View? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return null
            }
            if (sToastView == null) {
                sToastView = Toast.makeText(context.applicationContext, "", Toast.LENGTH_SHORT).view
            }
            return sToastView
        }

        override fun equals(other: Any?): Boolean {
            return this === other
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }
    }

    private class ToastFactoryImplQ : ToastFactory {
        override fun makeText(context: Context, text: CharSequence?, duration: Int): Toast {
            // 11 以上不允许去setView方式设置toastView，直接通过工厂方法创建toast
            return Toast.makeText(context, text, Toast.LENGTH_SHORT)
        }

        override fun equals(other: Any?): Boolean {
            return this === other
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }
    } //</editor-fold>
}
