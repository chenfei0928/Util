package io.github.chenfei0928.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import io.github.chenfei0928.reflect.parameterized.getParentParameterizedTypeClassDefinedImplInChild
import io.github.chenfei0928.viewbinding.inflateFunc

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-04 11:20
 */
abstract class BaseViewBindingFragment<VB : ViewBinding> : BaseFragment() {
    protected var viewBinding: VB? = null
        private set

    protected open val inflateFun: (LayoutInflater, ViewGroup?, Boolean) -> VB
        get() = getParentParameterizedTypeClassDefinedImplInChild<BaseViewBindingFragment<VB>, VB>(
            0
        ).inflateFunc()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        viewBinding = inflateFun(inflater, container, false)
        return viewBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewCreated(viewBinding!!, savedInstanceState)
    }

    protected abstract fun onViewCreated(view: VB, savedInstanceState: Bundle?)

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }
}
