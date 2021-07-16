package com.chenfei.util.mediaStore

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.chenfei.base.fragment.BaseFragment
import com.chenfei.util.kotlin.removeSelf

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
        post {
            resultCallback?.invoke(uri)
            removeSelf()
        }
    }
}

/**
 * 文件导入处理，返回文件的uri，可能需要使用[Context.getContentResolver]来读取
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-19 12:49
 */
typealias BaseFileImportUriFragment = BaseFileImportFragment<Uri>
