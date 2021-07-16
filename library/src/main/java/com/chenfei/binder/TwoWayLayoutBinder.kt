package com.chenfei.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.chenfei.adapter.ViewBindingHolder

/**
 * 用于提供接口以支持双向绑定
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-06 09:50
 */
abstract class TwoWayLayoutBinder<Bean, V : ViewBinding>(
    viewBindingInflater: (LayoutInflater, ViewGroup, Boolean) -> V,
) : BaseBindingBinder<Bean, V>(viewBindingInflater) {

    override fun onBindViewHolder(
        holder: ViewBindingHolder<Bean, V>, item: Bean, payloads: List<Any>,
    ) {
        super.onBindViewHolder(holder, item, payloads)
        // 同步view状态
        syncBeanChanged(holder, true)
        // 绑定view到bean
        bindTwoWayCallback(holder)
    }

    override fun onViewRecycled(holder: ViewBindingHolder<Bean, V>) {
        super.onViewRecycled(holder)
        // 解除bean到view的绑定
        unbindTwoWayCallback(holder)
    }

    /**
     * 同步View显示状态（从bean设置到view上）。
     * 由于binder的设计思想为双向绑定（当bean数据刷新后view自然被同步更新），故调用方在修改bean状态后可能不会通知适配器刷新，
     * 需要在此将bean的数据同步到view上。
     *
     * @param fromOnBind 如果本次调用方向是由可观察属性的变化通知到[holder]时为true。
     * 如果是[holder]其加载出来时同步状态，为false
     */
    protected abstract fun syncBeanChanged(
        holder: ViewBindingHolder<Bean, V>, fromOnBind: Boolean,
    )

    /**
     * 添加双向绑定回调
     * 当viewHolder被添加到window或进行填充内容时回调，通知该item将要被显示，需要对其状态进行监听
     */
    protected abstract fun bindTwoWayCallback(holder: ViewBindingHolder<Bean, V>)

    /**
     * 解除双向绑定回调
     * 在viewHolder被回收或从window中移除时会回调，以通知该item不在显示中
     */
    protected abstract fun unbindTwoWayCallback(holder: ViewBindingHolder<Bean, V>)
}
