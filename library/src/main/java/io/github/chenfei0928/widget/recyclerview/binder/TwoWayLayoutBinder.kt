package io.github.chenfei0928.widget.recyclerview.binder

import androidx.databinding.ListChanges
import androidx.databinding.Observable
import androidx.databinding.ObservableList
import androidx.databinding.ObservableMap
import androidx.lifecycle.LiveData
import io.github.chenfei0928.widget.recyclerview.adapter.ViewHolder

/**
 * 用于提供接口以支持双向绑定
 *
 * 只用来提供接口，不作为对外使用，外部使用时根据使用场景继承
 * [BaseEnvironmentTwoWayLayoutBinder]（当Bean没有ObserverField，只有外部监听时）
 * 或[BaseObservableTwoWayLayoutBinder]（当Bean有ObserverField时，同时提供外部监听）
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-06 09:50
 */
abstract class TwoWayLayoutBinder<Bean, VH : ViewHolder<Bean>>
    : BaseViewHolderBinder<Bean, VH>() {

    override fun onBindViewHolder(
        holder: VH, item: Bean, payloads: List<Any>,
    ) {
        super.onBindViewHolder(holder, item, payloads)
        // 同步view状态
        syncBeanChanged(holder, null, null)
    }

    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        // 绑定view到bean
        bindTwoWayCallback(holder)
    }

    override fun onViewDetachedFromWindow(holder: VH) {
        super.onViewDetachedFromWindow(holder)
        // 解除bean到view的绑定
        unbindTwoWayCallback(holder)
    }

    /**
     * 同步View显示状态（从bean设置到view上）。
     * 由于binder的设计思想为双向绑定（当bean数据刷新后view自然被同步更新），故调用方在修改bean状态后可能不会通知适配器刷新，
     * 需要在此将bean的数据同步到view上。
     *
     * @param sourceObservable 如果是来自于某项观察的属性发生了变化，则为变化了值的
     * [Observable]/[ObservableList]/[ObservableMap]。
     * 如果是[holder]其加载出来时同步状态则为[null]
     *
     * @param propertyId 根据[sourceObservable]类型不同：
     * - [null]时为[null]；
     * - [Observable.OnPropertyChangedCallback.onPropertyChanged]的propertyId；
     * - [ObservableList.OnListChangedCallback]为参数封装的[ListChanges]；
     * - [ObservableMap.OnMapChangedCallback.onMapChanged]的key；
     * - [LiveData]时为[null]；
     */
    protected abstract fun syncBeanChanged(
        holder: VH, sourceObservable: Any?, propertyId: Any?,
    )

    /**
     * 添加双向绑定回调
     * 当viewHolder被添加到window或进行填充内容时回调，通知该item将要被显示，需要对其状态进行监听
     */
    protected abstract fun bindTwoWayCallback(holder: VH)

    /**
     * 解除双向绑定回调
     * 在viewHolder被回收或从window中移除时会回调，以通知该item不在显示中
     */
    protected abstract fun unbindTwoWayCallback(holder: VH)
}
