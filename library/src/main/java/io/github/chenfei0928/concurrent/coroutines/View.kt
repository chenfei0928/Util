package io.github.chenfei0928.concurrent.coroutines

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.fragment.app.Fragment
import io.github.chenfei0928.base.ContextProvider
import io.github.chenfei0928.content.findActivity
import io.github.chenfei0928.view.findParentFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * 生命周期宿主的协程领域缓存
 */
private val viewOwnerCoroutineScopeCache: MutableMap<View, ViewCoroutineScope> = WeakHashMap()

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-10-19 17:01
 */
val View.attachedCoroutineScope: CoroutineScope
    get() = viewOwnerCoroutineScopeCache.getOrPut(this) {
        ViewCoroutineScope(this).init()
    }

private class ViewCoroutineScope(
    private val view: View
) : JobCoroutineScope(MainScope.coroutineContext) {
    override val coroutineContext: CoroutineContext
        get() = super.coroutineContext + // 生命周期的协程
                androidContextElement

    fun init(): ViewCoroutineScope = apply {
        view.doOnAttach {
            job.start()
            view.doOnDetach {
                cancel()
            }
        }
        launch {
            try {
                awaitCancellation()
            } finally {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    viewOwnerCoroutineScopeCache.remove(view, this@ViewCoroutineScope)
                } else {
                    viewOwnerCoroutineScopeCache.remove(view)
                }
            }
        }
    }

    private val androidContextElement: CoroutineAndroidContext = run {
        when (val host = view.findParentFragment() ?: view.context.findActivity() ?: view.context) {
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
}
