package io.github.chenfei0928.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * 根据生命周期缓存的字段委托，可以用于提供扩展字段的获取
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-11 15:09
 */
class LifecycleCacheDelegate<Owner : LifecycleOwner, V : LifecycleEventObserver>(
    /**
     * 传入宿主与值对象的关闭回调以创建值。
     * 值可以直接监听宿主生命周期变化以清理资源
     * （此时可通知回调，也可以不通知回调，因为关闭回调自身也会监听生命周期变化）
     * 值也可以在自己提前被关闭时调用回调通知委托移除它
     */
    private val valueCreator: (owner: Owner, closeCallback: () -> Unit) -> V
) : ReadOnlyProperty<Owner, V> {
    // 通过虚引用HashMap来持有键值对，其将自动移除GC不可访问的key和其所对应的value
    private val cache: MutableMap<Owner, V> = WeakHashMap()

    override fun getValue(thisRef: Owner, property: KProperty<*>): V {
        // 宿主存活时，创建或从缓存中获取一个与该宿主生命周期绑定的协程实例
        return cache.getOrPut(thisRef) {
            val closeCallback = CloseCallback(thisRef)
            // 传入宿主与值对象的关闭回调
            val observer = valueCreator(thisRef, closeCallback)
            closeCallback.observer = observer
            // 使其监听宿主生命周期变化
            thisRef.lifecycle.addObserver(observer)
            thisRef.lifecycle.addObserver(closeCallback)
            observer
        }
    }

    private inner class CloseCallback(
        private val owner: LifecycleOwner
    ) : () -> Unit, LifecycleEventObserver {
        lateinit var observer: LifecycleEventObserver

        override fun invoke() {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                cache.remove(owner, observer)
            } else {
                cache.remove(owner)
            }
            owner.lifecycle.removeObserver(observer)
            owner.lifecycle.removeObserver(this)
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                invoke()
            }
        }
    }
}
