package io.github.chenfei0928.webkit

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * WebView 预加载，提供webView的初始化、view替换
 *
 * @author chenf()
 * @date 2025-03-27 10:38
 */
abstract class WebViewPreloader<V : WebView>(
    context: Context,
    webViewPlaceHolder: View = View(context),
) {
    val webViewContainerView = FrameLayout(context).apply {
        addView(webViewPlaceHolder)
    }
    private var webViewLifecycleOwner: WebViewLifecycleOwner<V>? = null
    val webView: V?
        get() = webViewLifecycleOwner?.webView

    private val webViewPreloaderLifecycleOwner = object : LifecycleOwner {
        override val lifecycle = LifecycleRegistry(this)
    }

    protected val webViewConfig = object : WebViewSettingsUtil.ConfigWithCreator<V>(
        webViewPreloaderLifecycleOwner, webViewPlaceHolder
    ) {
        override fun create(context: Context): V =
            this@WebViewPreloader.create(context)

        override fun onWebViewCreated(
            webView: V, webViewLifecycleOwner: WebViewLifecycleOwner<V>
        ) {
            this@WebViewPreloader.webViewLifecycleOwner = webViewLifecycleOwner
            this@WebViewPreloader.onWebViewCreated(webView, webViewLifecycleOwner)
        }
    }

    protected abstract fun create(context: Context): V
    protected abstract fun onWebViewCreated(
        webView: V, webViewLifecycleOwner: WebViewLifecycleOwner<V>
    )

    open fun init() {
        webViewPreloaderLifecycleOwner.lifecycle.currentState = Lifecycle.State.CREATED
        WebViewSettingsUtil.installWebViewWithLifecycle(config = webViewConfig)
    }

    /**
     * 将WebView替换到目标[placeHolder]位置上，并让webView跟踪目标生命周期[lifecycleOwner]
     */
    fun placeInto(lifecycleOwner: LifecycleOwner, placeHolder: View) {
        (webViewContainerView.parent as? ViewGroup)
            ?.removeView(webViewContainerView)
        webViewContainerView.id = placeHolder.id
        // 替换到placeHolder占位View
        val parentView: ViewGroup = placeHolder.parent as ViewGroup
        val index = parentView.indexOfChild(placeHolder)
        parentView.removeView(placeHolder)
        val layoutParams = placeHolder.layoutParams
        if (layoutParams != null) {
            parentView.addView(webViewContainerView, index, layoutParams)
        } else {
            parentView.addView(webViewContainerView, index, null)
        }
        // 目标宿主生命周期销毁时将webView移除出父view，并在resume、pause生命周期时将状态透传给webView
        lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                // 替换到placeHolder占位View
                parentView.removeView(webViewContainerView)
                val layoutParams = webViewContainerView.layoutParams
                if (layoutParams != null) {
                    parentView.addView(placeHolder, index, layoutParams)
                } else {
                    parentView.addView(placeHolder, index)
                }
            } else {
                webViewPreloaderLifecycleOwner.lifecycle.currentState =
                    event.targetState
            }
        })
    }

    open fun release() {
        webViewPreloaderLifecycleOwner.lifecycle.currentState = Lifecycle.State.DESTROYED
    }

    companion object {
        inline fun <V : WebView> invoke(
            context: Context,
            crossinline creator: (Context) -> V,
            crossinline onWebViewCreated: (webView: V, webViewLifecycleOwner: WebViewLifecycleOwner<V>) -> Unit,
            webViewConfigSettings: (WebViewSettingsUtil.ConfigWithCreator<V>) -> Unit = {},
        ) = object : WebViewPreloader<V>(context.applicationContext) {
            override fun create(context: Context): V = creator(context)
            override fun onWebViewCreated(
                webView: V,
                webViewLifecycleOwner: WebViewLifecycleOwner<V>
            ) = onWebViewCreated(webView, webViewLifecycleOwner)
        }.apply {
            webViewConfigSettings(webViewConfig)
            init()
        }
    }
}
