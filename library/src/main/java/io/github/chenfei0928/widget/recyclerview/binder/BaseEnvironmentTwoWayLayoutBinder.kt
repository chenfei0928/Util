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
            EnvironmentObservableRegister.register(environments[index], callback)
        }
    }

    @CallSuper
    override fun unbindTwoWayCallback(holder: VH) {
        if (environments.isEmpty()) {
            return
        }
        // 取消监听环境变化
        holder.onEnvironmentChanged.forEachIndexed { index, callback ->
            EnvironmentObservableRegister.unregister(environments[index], callback)
        }
    }

    /**
     * View与切环境变化的监听
     */
    private val VH.onEnvironmentChanged: Array<Any> by ViewHolderTagValDelegate(
        R.id.onPropertyChanged
    ) { holder ->
        if (environments.isEmpty()) {
            emptyArray()
        } else environments.mapToArray { observer ->
            when (observer) {
                is Observable -> object : Observable.OnPropertyChangedCallback() {
                    override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                        syncBeanChanged(holder, observer, propertyId)
                    }
                }
                is ObservableList<*> -> object : ListChanges.ListCallback() {
                    override fun onNotifyCallback(
                        sender: ObservableList<Any>, changes: ListChanges
                    ) {
                        syncBeanChanged(holder, observer, changes)
                    }
                }
                is ObservableMap<*, *> -> object :
                    ObservableMap.OnMapChangedCallback<ObservableMap<Any, Any>, Any, Any>() {
                    override fun onMapChanged(sender: ObservableMap<Any, Any>?, key: Any?) {
                        syncBeanChanged(holder, observer, key)
                    }
                }
                is LiveData<*> -> Observer<Any> {
                    syncBeanChanged(holder, observer, null)
                }
                else -> createCallback(observer) ?: throw IllegalArgumentException(
                    "子类 ${this.javaClass.name} 未提供有效回调以注册，" +
                            "子类需对此类型 ${observer.javaClass.name} 返回回调，" +
                            "并重写 bindTwoWayCallback 与 unbindTwoWayCallback 方法注册回调"
                )
            }
        }
    }

    protected open fun createCallback(observer: Any): Any? {
        return null
    }
}
