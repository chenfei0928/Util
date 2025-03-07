package io.github.chenfei0928.view.asyncinflater

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import io.github.chenfei0928.concurrent.ExecutorAndCallback
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.util.Log

/**
 * 使用自定义的次线程执行器来替换其类的布局加载器，并简化逻辑
 * [androidx.asynclayoutinflater.view.AsyncLayoutInflater]
 *
 * @author MrFeng
 * @date 2018/4/10
 */
class AsyncLayoutInflater(
    context: Context,
    override val executorOrScope: ExecutorAndCallback = ExecutorUtil,
    override val inflater: LayoutInflater = BasicInflater(context),
) : IAsyncLayoutInflater {

    /**
     * 通过自定义的布局创建者在子线程创建子布局
     *
     * @param onCreateView 子视图创建者
     * @param parent       父布局，用于生成LayoutParam
     * @param callback     子视图创建完成后在主线程的回调
     * @param <V>          父布局的类型
     */
    @UiThread
    override fun <VG : ViewGroup, R> inflate(
        onCreateView: (LayoutInflater, VG) -> R,
        parent: VG,
        callback: (R) -> Unit
    ) = inflate({ onCreateView.invoke(inflater, parent) }, callback)

    /**
     * 通过布局文件创建者在子线程创建子布局
     *
     * @param resId    子视图布局id
     * @param parent   父布局，用于生成LayoutParam
     * @param callback 子视图创建完成后在主线程的回调
     * @param <V>      父布局的类型
     */
    @UiThread
    override fun <VG : ViewGroup> inflate(
        resId: Int,
        parent: VG?,
        callback: (View) -> Unit
    ) = inflate({ inflater.inflate(resId, parent, false) }, callback)

    private inline fun <V> inflate(
        crossinline inflate: () -> V,
        crossinline callback: (V) -> Unit
    ) = executorOrScope.execute({
        try {
            return@execute inflate()
        } catch (ex: RuntimeException) {
            // Probably a Looper failure, retry on the UI thread
            Log.w(TAG, run {
                "Failed to inflate resource in the background! Retrying on the UI thread"
            }, ex)
            return@execute null
        }
    }, { view ->
        callback(view ?: inflate())
    })

    companion object {
        private const val TAG = "KW_AsyncLayoutInflater"

        fun sync(context: Context) = AsyncLayoutInflater(
            context, ExecutorAndCallback.DirectExecutor,
            if (context is Activity) context.layoutInflater else LayoutInflater.from(context)
        )

        fun sync(fragment: Fragment) = AsyncLayoutInflater(
            fragment.requireContext(), ExecutorAndCallback.DirectExecutor, fragment.layoutInflater
        )

        fun sync(dialog: Dialog) = AsyncLayoutInflater(
            dialog.context, ExecutorAndCallback.DirectExecutor, dialog.layoutInflater
        )
    }
}
