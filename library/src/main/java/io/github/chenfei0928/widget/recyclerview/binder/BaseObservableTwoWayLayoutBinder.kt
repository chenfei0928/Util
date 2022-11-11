package io.github.chenfei0928.widget.recyclerview.binder

import androidx.annotation.CallSuper
import androidx.databinding.Observable
import io.github.chenfei0928.util.BeanExtValDelegate
import io.github.chenfei0928.widget.recyclerview.adapter.ViewHolder

/**
 * dataBinding [Observable] 的从实例状态回调通知view更新的实现
 * 要求子类实现[setBeanPropertyChangedNeedViewUpdateCallback]回调，将给定的监听器设置给bean的可监听字段，
 * 在字段值变化时通知给定的回调，回调中将会调用[syncBeanChanged]来更新view
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-06 11:12
 */
abstract class BaseObservableTwoWayLayoutBinder<Bean, VH : ViewHolder<Bean>>(
    environments: Array<Observable>,
) : BaseEnvironmentTwoWayLayoutBinder<Bean, VH>(environments) {

    @CallSuper
    override fun bindTwoWayCallback(holder: VH) {
        super.bindTwoWayCallback(holder)
        // 绑定view到bean
        holder.item?.viewObservable?.holder = holder
    }

    @CallSuper
    override fun unbindTwoWayCallback(holder: VH) {
        super.unbindTwoWayCallback(holder)
        // 解除bean到view的绑定
        holder.item?.viewObservable?.holder = null
    }

    //<editor-fold defaultstate="collapsed" desc="Bean的扩展属性以保存监听器">
    /**
     * 获取当前 可删除状态标记实例Bean 的 其状态变化时更新view回调 的get方法委托
     */
    private val Bean.viewObservable: ObservableBindToViewHolderCallback<Bean, VH>
            by object : BeanExtValDelegate<Bean, ObservableBindToViewHolderCallback<Bean, VH>>() {

                override fun create(thisRef: Bean): ObservableBindToViewHolderCallback<Bean, VH> {
                    val observable =
                        ObservableBindToViewHolderCallback(this@BaseObservableTwoWayLayoutBinder)
                    this@BaseObservableTwoWayLayoutBinder.setBeanPropertyChangedNeedViewUpdateCallback(
                        thisRef, observable
                    )
                    return observable
                }
            }

    /**
     * 某个Bean被标记为准备删除状态变化时到view点选图标状态的监听器
     * 其将会在变化时通知[syncBeanChanged]
     */
    private class ObservableBindToViewHolderCallback<Bean, VH : ViewHolder<Bean>>(
        private val binder: BaseObservableTwoWayLayoutBinder<Bean, VH>,
        var holder: VH? = null,
    ) : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            val holder = holder ?: return
            binder.syncBeanChanged(holder, sender, propertyId)
        }
    }
    //</editor-fold>

    /**
     * 对[bean]的可监听属性设置监听，当其更改后要通过[callback]通知view更新
     * 在字段值变化时通知给定的回调，回调中将会调用[syncBeanChanged]来更新view
     */
    protected abstract fun setBeanPropertyChangedNeedViewUpdateCallback(
        bean: Bean, callback: Observable.OnPropertyChangedCallback
    )
}
