package io.github.chenfei0928.webkit

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.webkit.ClientCertRequest
import android.webkit.HttpAuthHandler
import android.webkit.RenderProcessGoneDetail
import android.webkit.SafeBrowsingResponse
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import io.github.chenfei0928.os.safeHandler

/**
 * @author chenf()
 * @date 2025-03-21 17:48
 */
@Suppress("DEPRECATION")
open class WebViewClientWrapper(
    val impl: WebViewClient
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?, url: String?
    ): Boolean {
        return impl.shouldOverrideUrlLoading(view, url)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        return impl.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageStarted(
        view: WebView?,
        url: String?,
        favicon: Bitmap?
    ) {
        impl.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        impl.onPageFinished(view, url)
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        impl.onLoadResource(view, url)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPageCommitVisible(view: WebView?, url: String?) {
        impl.onPageCommitVisible(view, url)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        url: String?
    ): WebResourceResponse? {
        return impl.shouldInterceptRequest(view, url)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        return impl.shouldInterceptRequest(view, request)
    }

    override fun onTooManyRedirects(
        view: WebView?,
        cancelMsg: Message?,
        continueMsg: Message?
    ) {
        impl.onTooManyRedirects(view, cancelMsg, continueMsg)
    }

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        impl.onReceivedError(view, errorCode, description, failingUrl)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        impl.onReceivedError(view, request, error)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        impl.onReceivedHttpError(view, request, errorResponse)
    }

    override fun onFormResubmission(
        view: WebView?,
        dontResend: Message?,
        resend: Message?
    ) {
        impl.onFormResubmission(view, dontResend, resend)
    }

    override fun doUpdateVisitedHistory(
        view: WebView?,
        url: String?,
        isReload: Boolean
    ) {
        impl.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        impl.onReceivedSslError(view, handler, error)
    }

    override fun onReceivedClientCertRequest(
        view: WebView?,
        request: ClientCertRequest?
    ) {
        impl.onReceivedClientCertRequest(view, request)
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        impl.onReceivedHttpAuthRequest(view, handler, host, realm)
    }

    override fun shouldOverrideKeyEvent(
        view: WebView?,
        event: KeyEvent?
    ): Boolean {
        return impl.shouldOverrideKeyEvent(view, event)
    }

    override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
        impl.onUnhandledKeyEvent(view, event)
    }

    override fun onScaleChanged(
        view: WebView?,
        oldScale: Float,
        newScale: Float
    ) {
        impl.onScaleChanged(view, oldScale, newScale)
    }

    override fun onReceivedLoginRequest(
        view: WebView?,
        realm: String?,
        account: String?,
        args: String?
    ) {
        impl.onReceivedLoginRequest(view, realm, account, args)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRenderProcessGone(
        view: WebView?,
        detail: RenderProcessGoneDetail?
    ): Boolean {
        return impl.onRenderProcessGone(view, detail)
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onSafeBrowsingHit(
        view: WebView?,
        request: WebResourceRequest?,
        threatType: Int,
        callback: SafeBrowsingResponse?
    ) {
        impl.onSafeBrowsingHit(view, request, threatType, callback)
    }

    override fun equals(other: Any?): Boolean {
        return impl.equals(other)
    }

    override fun hashCode(): Int {
        return impl.hashCode()
    }

    override fun toString(): String {
        return impl.toString()
    }

    class RestartWhenRenderProcessGone<V : WebView>(
        impl: WebViewClient,
        private val webViewLifecycleOwner: WebViewLifecycleOwner<V>,
        private val config: WebViewSettingsUtil.ConfigWithCreator<V>,
    ) : WebViewClientWrapper(impl) {
        override fun onRenderProcessGone(
            view: WebView?,
            detail: RenderProcessGoneDetail?
        ): Boolean {
            val result = super.onRenderProcessGone(view, detail)
            Log.d(TAG, run {
                "onRenderProcessGone: impl result is $result, config: ${config.restartWebViewOnRenderGone}"
            })
            return when (config.restartWebViewOnRenderGone) {
                WebViewSettingsUtil.ConfigWithCreator.RENDER_GONE_NOOP -> result
                WebViewSettingsUtil.ConfigWithCreator.RENDER_GONE_CRASH -> {
                    false
                }
                WebViewSettingsUtil.ConfigWithCreator.RENDER_GONE_RECREATE_ACTIVITY -> {
                    val hostActivity = config.hostActivity
                    if (hostActivity != null) {
                        hostActivity.recreate()
                        true
                    } else {
                        Log.e(TAG, "onRenderProcessGone: recreateActivity but hostActivity is null")
                        result
                    }
                }
                WebViewSettingsUtil.ConfigWithCreator.RENDER_GONE_REUSE_HOST_FRAGMENT -> {
                    val f = config.hostFragment
                    if (f != null) {
                        f.parentFragmentManager.commit {
                            remove(f)
                            add(f.id, f, f.tag)
                        }
                        true
                    } else {
                        Log.e(TAG, "onRenderProcessGone: reuseFragment but hostFragment is null")
                        result
                    }
                }
                WebViewSettingsUtil.ConfigWithCreator.RENDER_GONE_RECREATE_HOST_FRAGMENT -> {
                    val f = config.hostFragment
                    if (f != null) {
                        f.parentFragmentManager.commit {
                            remove(f)
                            val newF = f.javaClass.newInstance()
                            newF.arguments = f.arguments
                            add(f.id, newF, f.tag)
                        }
                        true
                    } else {
                        Log.e(TAG, "onRenderProcessGone: recreateFragment but hostFragment is null")
                        result
                    }
                }
                WebViewSettingsUtil.ConfigWithCreator.RENDER_GONE_RECREATE_VIEW ->
                    if (webViewLifecycleOwner.lifecycle.currentState == Lifecycle.State.INITIALIZED) {
                        return true
                    } else {
                        config.lifecycleOwner.lifecycle.removeObserver(webViewLifecycleOwner)
                        webViewLifecycleOwner.onRenderProcessGone()
                        // post一个task，避免其它
                        config.lifecycleOwner.safeHandler.post {
                            WebViewSettingsUtil.installWebViewWithLifecycleImpl(config)
                        }
                        true
                    }
                else -> result
            }
        }
    }

    companion object {
        private const val TAG = "WebViewClientWrapper"
    }
}
