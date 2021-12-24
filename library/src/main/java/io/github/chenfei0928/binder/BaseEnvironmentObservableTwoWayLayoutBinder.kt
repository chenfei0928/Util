package io.github.chenfei0928.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.databinding.Observable
import androidx.viewbinding.ViewBinding
import io.github.chenfei0928.adapter.ViewBindingHolder
import io.github.chenfei0928.library.R
import io.github.chenfei0928.util.kotlin.ViewHolderTagDelegate
import io.github.chenfei0928.util.kotlin.createOnPropertyChanged

/**
 * 带有一个环境参数的view更新实现
 * 带有额外的外部依赖字段，该字段会控制所有子view的显示逻辑，其变化时也会通过[syncBeanChanged]回调
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-06 13:58
 */
abstract class BaseEnvironmentObservableTwoWayLayoutBinder<Bean, V : ViewBinding>(
    private val environment: Observable,
    viewBindingInflater: (LayoutInflater, ViewGroup, Boolean) -> V
) : BaseObservableTwoWayLayoutBinder<Bean, V>(viewBindingInflater) {

    /**
     * 子类需要按需重写本方法，并实现view更新
     */
    override fun onViewHolderCreated(holder: ViewBindingHolder<Bean, V>, parent: ViewGroup) {
        super.onViewHolderCreated(holder, parent)
        // 当环境变化时，更新数据的回调
        holder.onEnvironmentChanged = environment.createOnPropertyChanged {
            syncBeanChanged(holder, false)
        }
    }

    @CallSuper
    override fun bindTwoWayCallback(holder: ViewBindingHolder<Bean, V>) {
        super.bindTwoWayCallback(holder)
        // 监听环境变化
        holder.onEnvironmentChanged?.let {
            environment.addOnPropertyChangedCallback(it)
        }
    }

    @CallSuper
    override fun unbindTwoWayCallback(holder: ViewBindingHolder<Bean, V>) {
        super.unbindTwoWayCallback(holder)
        // 取消监听环境变化
        holder.onEnvironmentChanged?.let {
            environment.removeOnPropertyChangedCallback(it)
        }
    }

    /**
     * View与切环境变化的监听
     */
    private var ViewBindingHolder<Bean, V>.onEnvironmentChanged: Observable.OnPropertyChangedCallback? by ViewHolderTagDelegate(
        R.id.onPropertyChanged)
}
