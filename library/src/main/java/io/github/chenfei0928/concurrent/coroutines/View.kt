package io.github.chenfei0928.concurrent.coroutines

import android.view.View
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import java.util.WeakHashMap
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
    @Suppress("MagicNumber")
    get() = viewOwnerCoroutineScopeCache.getOrPut(this) {
        ViewCoroutineScope(this).init()
    } + CoroutineStackTraceRecordContextImpl(4)

private class ViewCoroutineScope(
    private val view: View
) : JobCoroutineScope(MainScope.coroutineContext) {
    private val androidContextElement: CoroutineAndroidContext =
        CoroutineAndroidContextImpl.newInstance(view)

    override val coroutineContext: CoroutineContext =
        super.coroutineContext + androidContextElement

    fun init(): ViewCoroutineScope = apply {
        view.doOnAttach {
            job.start()
            view.doOnDetach {
                cancel()
            }
        }
        onCancellation {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                viewOwnerCoroutineScopeCache.remove(view, this@ViewCoroutineScope)
            } else {
                viewOwnerCoroutineScopeCache.remove(view)
            }
        }
    }
}
