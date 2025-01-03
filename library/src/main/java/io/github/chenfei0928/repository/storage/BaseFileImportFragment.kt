package io.github.chenfei0928.repository.storage

import android.os.Bundle
import io.github.chenfei0928.app.fragment.removeSelf
import io.github.chenfei0928.base.fragment.BaseFragment
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.concurrent.UiTaskExecutor.Companion.runOnUiThread

/**
 * 抽离传入参数类型，以便于直接导入为文件或其他类型的解码器
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-23 15:48
 */
abstract class BaseFileImportFragment<T> : BaseFragment() {
    open var resultCallback: ((T?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 只有在有回调并且不是恢复现场时才启动文件选择
        if (resultCallback != null && savedInstanceState == null) {
            launchFileChoose()
        }
    }

    /**
     * 忽略回调、权限检查，直接启动文件选择
     */
    protected abstract fun launchFileChoose()

    /**
     * 提示用户是否成功保存，并移除自身
     */
    protected open fun removeSelf(uri: T?) {
        ExecutorUtil.runOnUiThread {
            resultCallback?.invoke(uri)
            removeSelf()
        }
    }
}
