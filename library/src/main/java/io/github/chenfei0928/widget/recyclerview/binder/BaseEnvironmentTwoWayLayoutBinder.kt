package io.github.chenfei0928.widget.recyclerview.binder

import androidx.annotation.CallSuper
import androidx.databinding.ListChanges
import androidx.databinding.Observable
import androidx.databinding.ObservableList
import androidx.databinding.ObservableMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.github.chenfei0928.collection.mapToArray
import io.github.chenfei0928.util.DependencyChecker
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
                callback.register()
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
                callback.unregister()
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
            createCallback(holder, observer) ?: when {
                observer is LiveData<*> ->
                    InternalCallback.LiveDataObserver(this, holder, observer)
                DependencyChecker.dataBinding && observer is Observable ->
                    InternalCallback.ObservableObserver(this, holder, observer)
                DependencyChecker.dataBinding && observer is ObservableList<*> ->
                    InternalCallback.ObservableListObserver(this, holder, observer)
                DependencyChecker.dataBinding && observer is ObservableMap<*, *> ->
                    InternalCallback.ObservableMapObserver(this, holder, observer)
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
    private sealed interface InternalCallback {
        fun register()
        fun unregister()

        class LiveDataObserver<Bean, VH : ViewHolder<Bean>, T>(
            private val binder: BaseEnvironmentTwoWayLayoutBinder<Bean, VH>,
            private val holder: VH,
            private val observer: LiveData<T>,
        ) : Observer<T?>, InternalCallback {
            override fun onChanged(value: T?) = binder.syncBeanChanged(holder, observer, null)
            override fun register() = observer.removeObserver(this)
            override fun unregister() = observer.observeForever(this)
        }

        class ObservableObserver<Bean, VH : ViewHolder<Bean>>(
            private val binder: BaseEnvironmentTwoWayLayoutBinder<Bean, VH>,
            private val holder: VH,
            private val observer: Observable,
        ) : Observable.OnPropertyChangedCallback(), InternalCallback {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) =
                binder.syncBeanChanged(holder, observer, propertyId)

            override fun register() = observer.addOnPropertyChangedCallback(this)
            override fun unregister() = observer.removeOnPropertyChangedCallback(this)
        }

        class ObservableListObserver<Bean, VH : ViewHolder<Bean>, T : Any>(
            private val binder: BaseEnvironmentTwoWayLayoutBinder<Bean, VH>,
            private val holder: VH,
            private val observer: ObservableList<T>,
        ) : ListChanges.ListCallback<T>(), InternalCallback {
            override fun onNotifyCallback(
                sender: ObservableList<T>, changes: ListChanges
            ) = binder.syncBeanChanged(holder, observer, changes)

            override fun register() = observer.addOnListChangedCallback(this)
            override fun unregister() = observer.removeOnListChangedCallback(this)
        }

        class ObservableMapObserver<Bean, VH : ViewHolder<Bean>, K, V>(
            private val binder: BaseEnvironmentTwoWayLayoutBinder<Bean, VH>,
            private val holder: VH,
            private val observer: ObservableMap<K, V>,
        ) : ObservableMap.OnMapChangedCallback<ObservableMap<K, V>, K, V>(),
            InternalCallback {
            override fun onMapChanged(sender: ObservableMap<K, V>?, key: K?) =
                binder.syncBeanChanged(holder, observer, key)

            override fun register() = observer.addOnMapChangedCallback(this)
            override fun unregister() = observer.removeOnMapChangedCallback(this)
        }
    }

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
