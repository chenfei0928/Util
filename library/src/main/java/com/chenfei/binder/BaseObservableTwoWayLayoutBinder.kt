package com.chenfei.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.databinding.Observable
import androidx.viewbinding.ViewBinding
import com.chenfei.adapter.ViewBindingHolder
import com.chenfei.util.kotlin.BeanExtValDelegate

/**
 * dataBinding [Observable] 的从实例状态回调通知view更新的实现
 * 要求子类实现[setBeanPropertyChangedNeedViewUpdateCallback]回调，将给定的监听器设置给bean的可监听字段，
 * 在字段值变化时通知给定的回调，回调中将会调用[syncBeanChanged]来更新view
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-06 11:12
 */
abstract class BaseObservableTwoWayLayoutBinder<Bean, V : ViewBinding>(
    viewBindingInflater: (LayoutInflater, ViewGroup, Boolean) -> V,
) : TwoWayLayoutBinder<Bean, V>(viewBindingInflater) {

    @CallSuper
    override fun bindTwoWayCallback(holder: ViewBindingHolder<Bean, V>) {
        // 绑定view到bean
        holder.item?.viewObservable?.holder = holder
    }

    @CallSuper
    override fun unbindTwoWayCallback(holder: ViewBindingHolder<Bean, V>) {
        // 解除bean到view的绑定
        holder.item?.viewObservable?.holder = null
    }

    //<editor-fold defaultstate="collapsed" desc="Bean的扩展属性以保存监听器">
    /**
     * 获取当前 可删除状态标记实例Bean 的 其状态变化时更新view回调 的get方法委托
     */
    private val Bean.viewObservable: ObservableBindToViewHolderCallback<Bean, V> by BeanViewObservable(
        this)

    private class BeanViewObservable<Bean, V : ViewBinding>(
        private val binder: BaseObservableTwoWayLayoutBinder<Bean, V>,
    ) : BeanExtValDelegate<Bean, ObservableBindToViewHolderCallback<Bean, V>>() {
        override fun create(thisRef: Bean): ObservableBindToViewHolderCallback<Bean, V> {
            val observable = ObservableBindToViewHolderCallback(binder)
            binder.setBeanPropertyChangedNeedViewUpdateCallback(thisRef, observable)
            return observable
        }
    }

    /**
     * 某个Bean被标记为准备删除状态变化时到view点选图标状态的监听器
     * 其将会在变化时通知[syncBeanChanged]
     */
    private class ObservableBindToViewHolderCallback<Bean, V : ViewBinding>(
        private val binder: BaseObservableTwoWayLayoutBinder<Bean, V>,
        var holder: ViewBindingHolder<Bean, V>? = null,
    ) : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            val holder = holder ?: return
            binder.syncBeanChanged(holder, false)
        }
    }
    //</editor-fold>

    /**
     * 对[bean]的可监听属性设置监听，当其更改后要通过[callback]通知view更新
     * 在字段值变化时通知给定的回调，回调中将会调用[syncBeanChanged]来更新view
     */
    abstract fun setBeanPropertyChangedNeedViewUpdateCallback(
        bean: Bean, callback: Observable.OnPropertyChangedCallback,
    )
}
