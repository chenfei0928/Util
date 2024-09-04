package io.github.chenfei0928.view.asyncinflater

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.UiThread
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.concurrent.ExecutorAndCallback
import io.github.chenfei0928.concurrent.coroutines.coroutineScope
import io.github.chenfei0928.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-10-24 15:36
 */
class SuspendLayoutInflater(
    context: Context,
    private val lifecycle: LifecycleOwner,
    override val inflater: LayoutInflater = BasicInflater(context),
) : IAsyncLayoutInflater {
    override val executor: ExecutorAndCallback = object : ExecutorAndCallback {
        override fun <R> execute(commend: () -> R, callback: (R) -> Unit) {
            inflate(commend, callback)
        }
    }
    override val executorOrScope: CoroutineScope
        get() = lifecycle.coroutineScope

    /**
     * 通过自定义的布局创建者在子线程创建子布局
     *
     * @param onCreateView 子视图创建者
     * @param parent       父布局，用于生成LayoutParam
     * @param callback     子视图创建完成后在主线程的回调
     * @param <VG>         父布局的类型
     */
    @UiThread
    override fun <VG : ViewGroup, R> inflate(
        onCreateView: (LayoutInflater, VG) -> R,
        parent: VG,
        callback: (R) -> Unit
    ) = inflate({ onCreateView(inflater, parent) }, callback)

    /**
     * 通过布局文件创建者在子线程创建子布局
     *
     * @param resId    子视图布局id
     * @param parent   父布局，用于生成LayoutParam
     * @param callback 子视图创建完成后在主线程的回调
     * @param <VG>     父布局的类型
     */
    @UiThread
    override fun <VG : ViewGroup> inflate(
        @LayoutRes resId: Int,
        parent: VG?,
        callback: (View) -> Unit
    ) = inflate({ inflater.inflate(resId, parent, false) }, callback)

    private inline fun <V> inflate(
        crossinline inflate: () -> V,
        crossinline callback: (V) -> Unit
    ) {
        lifecycle.coroutineScope.launch(Dispatchers.Main) {
            val view = withContext(Dispatchers.Default) {
                try {
                    inflate()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: RuntimeException) {
                    Log.w(TAG, run {
                        "Failed to inflate resource in the background! Retrying on the UI thread"
                    }, e)
                    null
                }
            } ?: inflate()
            if (isActive) {
                callback(view)
            }
        }
    }

    companion object {
        private const val TAG = "KW_SuspendLayoutInflate"
    }
}
