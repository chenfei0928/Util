package io.github.chenfei0928.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.databinding.Observable
import androidx.viewbinding.ViewBinding
import io.github.chenfei0928.adapter.ViewBindingHolder
import io.github.chenfei0928.util.R
import io.github.chenfei0928.widget.recyclerview.ViewHolderTagDelegate
import io.github.chenfei0928.util.kotlin.createOnPropertyChanged

/**
 * 带有一个环境参数的view更新实现
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-06 13:58
 */
abstract class BaseEnvironmentTwoWayLayoutBinder<Bean, V : ViewBinding>(
    private val environment: Observable,
    viewBindingInflater: (LayoutInflater, ViewGroup, Boolean) -> V,
) : TwoWayLayoutBinder<Bean, V>(viewBindingInflater) {

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
        // 监听环境变化
        holder.onEnvironmentChanged?.let {
            environment.addOnPropertyChangedCallback(it)
        }
    }

    @CallSuper
    override fun unbindTwoWayCallback(holder: ViewBindingHolder<Bean, V>) {
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
