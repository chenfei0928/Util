package io.github.chenfei0928.webkit

import android.view.View
import android.webkit.WebView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * @author chenf()
 * @date 2025-03-23 14:02
 */
class WebViewLifecycleOwner<V : WebView>(
    webView: V,
    placeHolder: View? = null
) : LifecycleOwner, LifecycleEventObserver {
    override val lifecycle: LifecycleRegistry = LifecycleRegistry(this)

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

    override fun onStateChanged(
        source: LifecycleOwner, event: Lifecycle.Event
    ) {
        lifecycle.currentState = event.targetState
    }
}
