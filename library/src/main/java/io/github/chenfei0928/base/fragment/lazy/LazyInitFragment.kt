package io.github.chenfei0928.base.fragment.lazy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isNotEmpty
import io.github.chenfei0928.view.asyncinflater.AsyncLayoutInflater
import io.github.chenfei0928.view.asyncinflater.IAsyncLayoutInflater

/**
 * 子view的懒加载fragment，允许子view在次线程中被实例化，建议用于子布局加载耗时较长的场景
 */
abstract class LazyInitFragment(
    loadInMainThread: Boolean = false
) : BaseDoubleCheckLazyInitFragment() {
    @Volatile
    private var asyncInflateDid = false
    open val asyncLayoutInflater: IAsyncLayoutInflater? by lazy(LazyThreadSafetyMode.NONE) {
        if (loadInMainThread) null else AsyncLayoutInflater(requireContext())
    }

    final override fun checkInflateImpl() {
        val layout = view
        if (layout == null || layout.isNotEmpty()) {
            return
        }
        val asyncLayoutInflater = asyncLayoutInflater
        if (asyncLayoutInflater == null) {
            // 在主线程使用同步方式加载
            onCreateViewImpl(layoutInflater, layout, lazyLoadSavedInstanceState).let { view ->
                layout.addView(view)
                onViewCreatedImpl(view, lazyLoadSavedInstanceState)
                lazyLoadSavedInstanceState = null
            }
        } else if (!asyncInflateDid) {
            asyncInflateDid = true
            // 异步子线程加载
            asyncLayoutInflater.inflate({ layoutInflater, vg ->
                onCreateViewImpl(layoutInflater, vg, lazyLoadSavedInstanceState)
            }, layout, {
                layout.addView(it)
                onViewCreatedImpl(it, lazyLoadSavedInstanceState)
                lazyLoadSavedInstanceState = null
            })
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
