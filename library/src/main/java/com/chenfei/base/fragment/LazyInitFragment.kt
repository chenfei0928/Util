package com.chenfei.base.fragment

import android.os.Build
import android.os.Bundle
import android.view.Choreographer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.chenfei.lib_base.R
import com.chenfei.view.asyncinflater.AsyncLayoutInflater

/**
 * 懒初始化内容的fragment
 */
abstract class BaseLazyInitFragment : BaseFragment() {
    internal var lazyLoadSavedInstanceState: Bundle? = null

    final override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        this.lazyLoadSavedInstanceState = savedInstanceState
        return FrameLayout(requireContext()).apply {
            id = R.id.lazyInitPlaceHolder
        }
    }

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

    /**
     * 检查子view是否已经载入，如未载入子view，将其载入
     */
    internal abstract fun checkInflate()

    /**
     * 检查是否可以加载子内容
     */
    private fun checkAllowInflate(): Boolean = host != null && isResumed
}

/**
 * 双重校验懒初始化子内容的fragment
 */
abstract class BaseDoubleCheckLazyInitFragment : BaseLazyInitFragment() {
    final override fun checkInflate() {
        if (isResumed && view != null && !isInflated) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Choreographer
                    .getInstance()
                    .postFrameCallback {
                        checkInflateImpl()
                    }
            } else {
                postDelayed(30) {
                    checkInflateImpl()
                }
            }
        }
    }

    internal abstract fun checkInflateImpl()

    abstract val isInflated: Boolean
}

/**
 * 子view的懒加载fragment，允许子view在次线程中被实例化，建议用于子布局加载耗时较长的场景
 */
abstract class LazyInitFragment(
    private val loadInMainThread: Boolean = false
) : BaseDoubleCheckLazyInitFragment() {
    @Volatile
    private var asyncInflateDid = false

    final override fun checkInflateImpl() {
        val layout = view
        if (layout != null && layout.childCount == 0) {
            if (loadInMainThread) {
                // 在主线程使用同步方式加载
                val view = onCreateViewImpl(layoutInflater, layout, lazyLoadSavedInstanceState)
                view?.let {
                    layout.addView(view)
                    onViewCreatedImpl(view, lazyLoadSavedInstanceState)
                    lazyLoadSavedInstanceState = null
                }
            } else if (!asyncInflateDid) {
                // 异步子线程加载
                AsyncLayoutInflater(layout.context).inflate({ layoutInflater, vg ->
                    onCreateViewImpl(layoutInflater, vg, lazyLoadSavedInstanceState)
                }, layout, {
                    // 是否存活的检查
                    if (isDestroyed || isInflated) {
                        return@inflate
                    }
                    layout.addView(it)
                    onViewCreatedImpl(it, lazyLoadSavedInstanceState)
                    lazyLoadSavedInstanceState = null
                })
                asyncInflateDid = true
            }
        }
    }

    override val isInflated: Boolean
        get() = view.let { layout ->
            layout != null && layout.childCount != 0
        }

    abstract fun onCreateViewImpl(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View?

    abstract fun onViewCreatedImpl(view: View, savedInstanceState: Bundle?)
}

/**
 * 子fragment的懒加载fragment，只会延时载入子fragment
 * 子fragment的view加载由fragment框架负责在主线程中初始化
 */
abstract class LazyInitInnerFragment<F : Fragment> : BaseDoubleCheckLazyInitFragment() {
    val fragment: F by lazy {
        childFragmentManager.findFragmentByTag(FRAGMENT_TAG) as? F ?: createFragment()
    }

    final override fun checkInflateImpl() {
        if (!isInflated) {
            childFragmentManager
                .beginTransaction()
                .add(R.id.lazyInitPlaceHolder, fragment, FRAGMENT_TAG)
                .commitNowAllowingStateLoss()
        }
    }

    override val isInflated: Boolean
        get() = fragment.isAdded

    abstract fun createFragment(): F

    companion object {
        private const val FRAGMENT_TAG = "fragment"
    }
}
