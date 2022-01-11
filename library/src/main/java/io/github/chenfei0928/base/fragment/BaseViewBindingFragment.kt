package io.github.chenfei0928.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.chenfei0928.reflect.parameterized.getParentParameterizedTypeClassDefinedImplInChild
import io.github.chenfei0928.util.inflateFunc

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-04 11:20
 */
abstract class BaseViewBindingFragment0<ViewBinding : androidx.viewbinding.ViewBinding> :
    BaseFragment() {
    protected var viewBinding: ViewBinding? = null

    protected abstract val inflateFun: (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding

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

    protected abstract fun onViewCreated(view: ViewBinding, savedInstanceState: Bundle?)

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }
}

abstract class BaseViewBindingFragment<ViewBinding : androidx.viewbinding.ViewBinding> :
    BaseViewBindingFragment0<ViewBinding>() {

    override val inflateFun: (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding
        get() = getParentParameterizedTypeClassDefinedImplInChild<BaseViewBindingFragment0<ViewBinding>, ViewBinding>(
            0
        ).inflateFunc()
}
