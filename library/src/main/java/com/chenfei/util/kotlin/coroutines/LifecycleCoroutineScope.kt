package com.chenfei.util.kotlin.coroutines

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.chenfei.base.app.BaseApplication
import com.chenfei.util.kotlin.isAlive
import kotlinx.coroutines.CoroutineScope
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

/**
 * 被取消的协程实例，在该实例上创建的协程子任务将永远不会被执行。
 */
private val cancelledCoroutineScope by lazy(LazyThreadSafetyMode.NONE) {
    JobCoroutineScope(MainScope.coroutineContext).apply {
        job.cancel()
    }
}

/**
 * 生命周期宿主的协程领域缓存
 */
private val lifecycleOwnerCoroutineScopeCache: MutableMap<LifecycleOwner, LifecycleCoroutineScope> =
    mutableMapOf()

/**
 * 使用生命周期宿主获取与该生命周期绑定的协程实例，其会根据宿主的生命周期结束时被取消。
 * 在宿主生命周期结束后再获取协程时，将返回一个被取消的协程实例，在该实例上创建的协程子任务将不会被执行。
 */
val LifecycleOwner.coroutineScope: CoroutineScope
    get() = if (this.lifecycle.isAlive) {
        // 宿主存活时，创建或从缓存中获取一个与该宿主生命周期绑定的协程实例
        lifecycleOwnerCoroutineScopeCache.getOrPut(this) {
            val coroutineScope = LifecycleCoroutineScope(this)
            this.lifecycle.addObserver(coroutineScope)
            coroutineScope
        }
    } else {
        // 宿主已经不在存活，其不应该再被执行任何任务
        cancelledCoroutineScope
    }

/**
 * 与生命周期绑定的协程上下文，带有异常处理
 * [博文](https://juejin.im/post/5cfb38f96fb9a07eeb139a00)
 */
private class LifecycleCoroutineScope(
    private val host: LifecycleOwner
) : JobCoroutineScope(MainScope.coroutineContext), LifecycleEventObserver, Closeable {
    override val coroutineContext: CoroutineContext
        get() = super.coroutineContext + // 生命周期的协程
                androidContextElement

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            // 关闭页面后，结束所有协程任务
            close()
        }
    }

    private val androidContextElement: CoroutineAndroidContext = run {
        when (host) {
            is Fragment -> {
                CoroutineAndroidContextImpl(host.activity ?: host.requireContext(), host)
            }
            is Activity -> {
                CoroutineAndroidContextImpl(host, null)
            }
            is Context -> {
                CoroutineAndroidContextImpl(host, null)
            }
            else -> {
                CoroutineAndroidContextImpl(BaseApplication.getInstance(), null)
            }
        }
    }

    /**
     * 关闭该协程，将自身从缓存中移除，并不再监听宿主的生命周期
     */
    override fun close() {
        job.cancel()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            lifecycleOwnerCoroutineScopeCache.remove(host, this)
        } else {
            lifecycleOwnerCoroutineScopeCache.remove(host)
        }
        host.lifecycle.removeObserver(this)
    }
}
