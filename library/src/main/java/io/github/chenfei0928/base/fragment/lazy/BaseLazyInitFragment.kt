package io.github.chenfei0928.base.fragment.lazy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import io.github.chenfei0928.base.fragment.BaseFragment
import io.github.chenfei0928.util.R

/**
 * 懒初始化内容的fragment
 */
abstract class BaseLazyInitFragment : BaseFragment() {
    internal var lazyLoadSavedInstanceState: Bundle? = null
    private var isDestroyed = false

    @Deprecated(
        message = "Use onCreateViewImpl",
        replaceWith = ReplaceWith("onCreateViewImpl(inflater, container, savedInstanceState)"),
        level = DeprecationLevel.ERROR
    )
    final override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        isDestroyed = false
        this.lazyLoadSavedInstanceState = savedInstanceState
        return FrameLayout(requireContext()).apply {
            id = R.id.lazyInitPlaceHolder
        }
    }

    @Deprecated(
        message = "Use onViewCreatedImpl",
        replaceWith = ReplaceWith("onViewCreatedImpl(view, savedInstanceState)"),
        level = DeprecationLevel.ERROR
    )
    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.layoutParams?.run {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        if (checkAllowInflate()) {
            checkInflate()
        }
    }

    override fun getView(): FrameLayout? {
        return super.getView() as FrameLayout?
    }

    override fun onResume() {
        super.onResume()
        if (checkAllowInflate()) {
            checkInflate()
        }
    }

    override fun onDestroyView() {
        isDestroyed = true
        super.onDestroyView()
    }

    /**
     * 检查子view是否已经载入，如未载入子view，将其载入
     */
    internal abstract fun checkInflate()

    /**
     * 检查是否可以加载子内容
     */
    private fun checkAllowInflate(): Boolean = host != null && isResumed
}
