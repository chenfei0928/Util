package io.github.chenfei0928.lifecycle

import android.util.Log
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.concurrent.UiTaskExecutor.Companion.runOnUiThread
import java.io.Closeable
import java.util.WeakHashMap
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * 根据生命周期缓存的字段委托，可以用于提供扩展字段的获取
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-11 15:09
 */
open class LifecycleCacheDelegate<Owner : LifecycleOwner, V : LifecycleEventObserver>(
    /**
     * 如果要在宿主死亡后固定返回一个失效对象，传递不为null的此值
     */
    private val deadValue: V? = null,
    /**
     * 传入宿主与值对象的关闭回调以创建值 [V]。
     *
     * 返回值必须直接监听宿主生命周期变化以清理资源。
     * 也允许返回值在自己提前被关闭时调用回调通知委托移除它
     */
    private val valueCreator: (owner: Owner, closeCallback: Closeable) -> V
) : ReadOnlyProperty<Owner, V> {
    // 通过虚引用HashMap来持有键值对，其将自动移除GC不可访问的key和其所对应的value
    private val cache: MutableMap<Owner, V> = WeakHashMap()

    override fun getValue(thisRef: Owner, property: KProperty<*>): V {
        if (!thisRef.lifecycle.isAlive && deadValue != null) {
            return deadValue
        }
        // 宿主存活时，创建或从缓存中获取一个与该宿主生命周期绑定的协程实例
        return cache.getOrPut(thisRef) {
            val closeCallback = CloseCallback(thisRef)
            // 传入宿主与值对象的关闭回调
            val observer = valueCreator(thisRef, closeCallback)
            closeCallback.observer = observer
            // 使其监听宿主生命周期变化
            val lifecycle = thisRef.lifecycle
            ExecutorUtil.runOnUiThread {
                lifecycle.addObserver(observer)
            }
            observer
        }
    }

    private inner class CloseCallback(
        private val owner: LifecycleOwner
    ) : Closeable {
        lateinit var observer: LifecycleEventObserver

        override fun close() {
            Log.d(TAG, "close: $observer because $owner was close.", RuntimeException())
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                cache.remove(owner, observer)
            } else {
                cache.remove(owner)
            }
        }
    }

    companion object {
        private const val TAG = "KW_LifecycleCacheD"
    }
}
