package io.github.chenfei0928.base.fragment.lazy

import android.view.Choreographer

/**
 * 双重校验懒初始化子内容的fragment
 */
abstract class BaseDoubleCheckLazyInitFragment : BaseLazyInitFragment() {
    final override fun checkInflate() {
        if (isResumed && view != null && !isInflated) {
            Choreographer
                .getInstance()
                .postFrameCallback {
                    checkInflateImpl()
                }
        }
    }

    internal abstract fun checkInflateImpl()

    abstract val isInflated: Boolean
}
