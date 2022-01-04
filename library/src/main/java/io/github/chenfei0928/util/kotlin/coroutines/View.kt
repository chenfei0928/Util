package io.github.chenfei0928.util.kotlin.coroutines

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.fragment.app.Fragment
import io.github.chenfei0928.base.ContextProvider
import io.github.chenfei0928.coroutines.CoroutineAndroidContext
import io.github.chenfei0928.coroutines.CoroutineAndroidContextImpl
import io.github.chenfei0928.coroutines.JobCoroutineScope
import io.github.chenfei0928.coroutines.MainScope
import io.github.chenfei0928.util.kotlin.findActivity
import io.github.chenfei0928.util.kotlin.findParentFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

/**
 * 生命周期宿主的协程领域缓存
 */
private val viewOwnerCoroutineScopeCache: MutableMap<View, ViewCoroutineScope> = mutableMapOf()

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-10-19 17:01
 */
val View.attachedCoroutineScope: CoroutineScope
    get() = viewOwnerCoroutineScopeCache.getOrPut(this) {
        ViewCoroutineScope(this)
    }

private class ViewCoroutineScope(
    private val view: View
) : JobCoroutineScope(MainScope.coroutineContext), Closeable {
    override val coroutineContext: CoroutineContext
        get() = super.coroutineContext + // 生命周期的协程
                androidContextElement

    init {
        view.doOnAttach {
            job.start()
            view.doOnDetach {
                cancel()
            }
        }
    }

    private val androidContextElement: CoroutineAndroidContext = run {
        when (val host: Any =
            view.findParentFragment() ?: view.context.findActivity() ?: view.context) {
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
                CoroutineAndroidContextImpl(ContextProvider.context, null)
            }
        }
    }

    /**
     * 关闭该协程，将自身从缓存中移除，并不再监听宿主的生命周期
     */
    override fun close() {
        job.cancel()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            viewOwnerCoroutineScopeCache.remove(view, this)
        } else {
            viewOwnerCoroutineScopeCache.remove(view)
        }
    }
}
