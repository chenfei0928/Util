package io.github.chenfei0928.webkit

import android.util.Log
import android.view.View
import android.webkit.WebView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * WebView 的生命周期宿主。
 * 同时可以直接当作生命周期时间监听器注册给宿主的生命周期，以自动完成调用WebView的生命周期事件。
 *
 * 同时作为生命周期宿主可以供其他位置来监听WebView控件的生命周期。
 *
 * 当前类不要有 `TAG` 字段，否则在 [io.github.chenfei0928.concurrent.coroutines.CoroutineAndroidContextImpl.tag]
 * 中可能会直接获取到当前类的tag，而非宿主的tag
 *
 * @author chenf()
 * @date 2025-03-23 14:02
 */
class WebViewLifecycleOwner<V : WebView> constructor(
    val webView: V,
    placeHolder: View? = null
) : LifecycleOwner, LifecycleEventObserver {
    override val lifecycle: Lifecycle
        private field = LifecycleRegistry(this)

    init {
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    webView.onResume()
                    webView.resumeTimers()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    webView.pauseTimers()
                    webView.onPause()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    WebViewSettingsUtil.onDestroy(webView, placeHolder)
                }
                else -> {
                    // noop
                }
            }
        })
    }

    fun onRenderProcessGone() {
        Log.v(TAG, "onRenderProcessGone: ${lifecycle.currentState}")
        lifecycle.currentState = Lifecycle.State.DESTROYED
    }

    override fun onStateChanged(
        source: LifecycleOwner, event: Lifecycle.Event
    ) {
        Log.v(TAG, "onStateChanged: $webView from ${lifecycle.currentState} to $event")
        if (lifecycle.currentState == Lifecycle.State.DESTROYED)
            return
        lifecycle.currentState = event.targetState
    }

    companion object {
        private const val TAG = "WebViewLifecycleOwner"
    }
}
