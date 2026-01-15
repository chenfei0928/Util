package io.github.chenfei0928.concurrent.coroutines

import androidx.annotation.ReturnThis
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.github.chenfei0928.lifecycle.EventLifecycleOwner
import io.github.chenfei0928.lifecycle.LifecycleCacheDelegate
import io.github.chenfei0928.lifecycle.isAlive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import java.io.Closeable
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * 被取消的协程实例，在该实例上创建的协程子任务将永远不会被执行。
 */
private val cancelledCoroutineScope by lazy(LazyThreadSafetyMode.NONE) {
    LifecycleCoroutineScope(EventLifecycleOwner.immortal) {}.apply {
        job.cancel()
    }
}

/**
 * 使用生命周期宿主获取与该生命周期绑定的协程实例，其会根据宿主的生命周期结束时被取消。
 * 在宿主生命周期结束后再获取协程时，将返回一个被取消的协程实例，在该实例上创建的协程子任务将不会被执行。
 *
 * 在 [Fragment] 中使用时，需要额外留意 [LifecycleOwner] 是 [Fragment] 本体还是 [Fragment.getViewLifecycleOwner]。
 * 会影响返回的协程作用域的生命周期而导致内存泄漏。
 *
 * 相较 [lifecycleScope] 添加了 [UncaughtLogCoroutineExceptionHandler] 和 [CoroutineAndroidContext]
 */
val LifecycleOwner.coroutineScope: CoroutineScope by
object : ReadOnlyProperty<LifecycleOwner, CoroutineScope> {
    private val delegate: LifecycleCacheDelegate<LifecycleOwner, LifecycleCoroutineScope> =
        LifecycleCacheDelegate { owner, closeCallback ->
            if (owner.lifecycle.isAlive) {
                // 宿主存活时，创建或从缓存中获取一个与该宿主生命周期绑定的协程实例
                LifecycleCoroutineScope(owner, closeCallback).init()
            } else {
                // 宿主已经不在存活，其不应该再被执行任何任务
                cancelledCoroutineScope
            }
        }

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): CoroutineScope {
        @Suppress("MagicNumber")
        return delegate.getValue(thisRef, property) + CoroutineStackTraceRecordContextImpl(5)
    }
}

/**
 * 与生命周期绑定的协程上下文，带有异常处理
 * [博文](https://juejin.im/post/5cfb38f96fb9a07eeb139a00)
 */
private class LifecycleCoroutineScope(
    host: LifecycleOwner,
    private val closeCallback: Closeable
) : JobCoroutineScope(MainScope.coroutineContext), LifecycleEventObserver {
    private val androidContextElement: CoroutineAndroidContext =
        CoroutineAndroidContextImpl.newInstance(host)

    override val coroutineContext: CoroutineContext =
        super.coroutineContext + androidContextElement

    @ReturnThis
    fun init(): LifecycleCoroutineScope = apply {
        onCancellation {
            closeCallback.close()
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            // 关闭页面后，结束所有协程任务
            cancel()
        }
    }
}
