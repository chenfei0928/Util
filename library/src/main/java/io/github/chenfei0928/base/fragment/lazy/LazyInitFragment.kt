package io.github.chenfei0928.base.fragment.lazy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.chenfei0928.view.asyncinflater.AsyncLayoutInflater

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
                onCreateViewImpl(layoutInflater, layout, lazyLoadSavedInstanceState).let { view ->
                    layout.addView(view)
                    onViewCreatedImpl(view, lazyLoadSavedInstanceState)
                    lazyLoadSavedInstanceState = null
                }
            } else if (!asyncInflateDid) {
                // 异步子线程加载
                AsyncLayoutInflater(layout.context).inflate({ layoutInflater, vg ->
                    onCreateViewImpl(layoutInflater, vg, lazyLoadSavedInstanceState)
                }, layout, {
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
    ): View

    abstract fun onViewCreatedImpl(view: View, savedInstanceState: Bundle?)
}
