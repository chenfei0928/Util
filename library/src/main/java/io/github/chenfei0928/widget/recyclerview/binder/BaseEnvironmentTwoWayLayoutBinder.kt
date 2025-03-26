package io.github.chenfei0928.widget.recyclerview.binder

import androidx.annotation.CallSuper
import androidx.databinding.ListChanges
import androidx.databinding.Observable
import androidx.databinding.ObservableList
import androidx.databinding.ObservableMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.github.chenfei0928.collection.mapToArray
import io.github.chenfei0928.util.R
import io.github.chenfei0928.widget.recyclerview.ViewHolderTagValDelegate
import io.github.chenfei0928.widget.recyclerview.adapter.ViewHolder

/**
 * 带有一个环境参数的view更新实现
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-06 13:58
 */
abstract class BaseEnvironmentTwoWayLayoutBinder<Bean, VH : ViewHolder<Bean>>(
    private val environments: Array<Any>,
) : TwoWayLayoutBinder<Bean, VH>() {

    @CallSuper
    override fun bindTwoWayCallback(holder: VH) {
        if (environments.isEmpty()) {
            return
        }
        // 监听环境变化
        holder.onEnvironmentChanged.forEachIndexed { index, callback ->
            if (callback is InternalCallback) {
                EnvironmentObservableRegister.register(environments[index], callback)
            }
        }
    }

    @CallSuper
    override fun unbindTwoWayCallback(holder: VH) {
        if (environments.isEmpty()) {
            return
        }
        // 取消监听环境变化
        holder.onEnvironmentChanged.forEachIndexed { index, callback ->
            if (callback is InternalCallback) {
                EnvironmentObservableRegister.unregister(environments[index], callback)
            }
        }
    }

    /**
     * View与切环境变化的监听
     */
    private val VH.onEnvironmentChanged: Array<Any> by ViewHolderTagValDelegate(
        R.id.cf0928util_onPropertyChanged
    ) { holder ->
        if (environments.isEmpty()) {
            emptyArray()
        } else environments.mapToArray { observer ->
            createCallback(holder, observer) ?: when (observer) {
                is Observable -> object : Observable.OnPropertyChangedCallback(), InternalCallback {
                    override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                        syncBeanChanged(holder, observer, propertyId)
                    }
                }
                is ObservableList<*> -> object : ListChanges.ListCallback(), InternalCallback {
                    override fun onNotifyCallback(
                        sender: ObservableList<Any>, changes: ListChanges
                    ) {
                        syncBeanChanged(holder, observer, changes)
                    }
                }
                is ObservableMap<*, *> -> object : InternalCallback,
                    ObservableMap.OnMapChangedCallback<ObservableMap<Any, Any>, Any, Any>() {
                    override fun onMapChanged(sender: ObservableMap<Any, Any>?, key: Any?) {
                        syncBeanChanged(holder, observer, key)
                    }
                }
                is LiveData<*> -> object : Observer<Any?>, InternalCallback {
                    override fun onChanged(value: Any?) {
                        syncBeanChanged(holder, observer, null)
                    }
                }
                else -> throw IllegalArgumentException(
                    "子类 ${this.javaClass.name} 未提供有效回调以注册，" +
                            "子类需对此类型 ${observer.javaClass.name} 返回回调，" +
                            "并重写 bindTwoWayCallback 与 unbindTwoWayCallback 方法注册回调"
                )
            }
        }
    }

    /**
     * 标记某个监听类型是 [onEnvironmentChanged] 创建的，而非子类重写 [createCallback] 方法创建
     */
    private interface InternalCallback

    /**
     * 创建指定 [environments] 的回调，并需要在回调中调用 [syncBeanChanged] 方法进行同步数据的更新
     *
     * 如果没有特别的需要，在当前类的 [onEnvironmentChanged] 中已提供了对 [Observable]、[ObservableList]、
     * [ObservableMap]、[LiveData] 的回调类型创建，也可以重写该方法处理其他类型的监听创建，
     * 或覆盖对 [onEnvironmentChanged] 的回调的处理。
     * 对于以上四种类型之外的回调的创建，也同时需要重写 [bindTwoWayCallback]、[unbindTwoWayCallback] 来注册回调
     */
    protected open fun createCallback(holder: VH, observer: Any): Any? {
        return null
    }
}
