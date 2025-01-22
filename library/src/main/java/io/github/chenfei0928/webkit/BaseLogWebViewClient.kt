package io.github.chenfei0928.webkit

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.SafeBrowsingResponseCompat
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebResourceRequestCompat
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewFeature
import io.github.chenfei0928.util.Log

/**
 * @author chenf()
 * @date 2024-11-12 17:40
 */
open class BaseLogWebViewClient : WebViewClientCompat() {

    /**
     * 接收到错误信息时触发
     * [ApiDocs](https://developer.android.com/reference/android/webkit/WebViewClient.html#onReceivedError(android.webkit.WebView,%20android.webkit.WebResourceRequest,%20android.webkit.WebResourceError))
     *
     * Added in API level 23
     */
    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView, request: WebResourceRequest, error: WebResourceErrorCompat
    ) {
        super.onReceivedError(view, request, error)
        debugWebViewMessage(
            "onReceivedError", view,
            request = request,
            error = error
        )
    }

    /**
     * 安全浏览提醒
     * [ApiDocs](https://developer.android.com/reference/android/webkit/WebViewClient.html#onSafeBrowsingHit(android.webkit.WebView,%20android.webkit.WebResourceRequest,%20int,%20android.webkit.SafeBrowsingResponse))
     *
     * Added in API level 27
     */
    @TargetApi(Build.VERSION_CODES.O_MR1)
    override fun onSafeBrowsingHit(
        view: WebView,
        request: WebResourceRequest,
        threatType: Int,
        callback: SafeBrowsingResponseCompat
    ) {
        super.onSafeBrowsingHit(view, request, threatType, callback)
        debugWebViewMessage(
            "onSafeBrowsingHit", view,
            request = request,
            threatType = threatType,
        )
    }

    /**
     * http请求错误的回调
     * [ApiDocs](https://developer.android.com/reference/android/webkit/WebViewClient.html#onReceivedHttpError(android.webkit.WebView,%20android.webkit.WebResourceRequest,%20android.webkit.WebResourceResponse))
     *
     * Added in API level 23
     */
    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceivedHttpError(
        view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        debugWebViewMessage(
            "onReceivedHttpError", view,
            request = request,
            errorResponse = errorResponse,
        )
    }

    /**
     * 接收到ssl证书错误的回调
     *
     * Added in API level 8
     */
    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError?) {
        super.onReceivedSslError(view, handler, error)
        debugWebViewMessage(
            "onReceivedSslError", view,
            sslError = error,
        )
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        debugWebViewMessage("shouldOverrideUrlLoading", view, request = request)
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (debugLog) {
            Log.v(TAG, "onPageStarted: $url")
        }
    }

    override fun onPageFinished(view: WebView, url: String) {
        if (debugLog) {
            Log.v(TAG, "onPageFinished: $url")
        }
        super.onPageFinished(view, url)
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        super.onLoadResource(view, url)
        if (debugLog) {
            Log.v(TAG, "onLoadResource: $url")
        }
    }

    override fun shouldInterceptRequest(
        view: WebView, request: WebResourceRequest
    ): WebResourceResponse? {
        debugWebViewMessage("shouldInterceptRequest", view, request = request)
        return super.shouldInterceptRequest(view, request)
    }

    //<editor-fold desc="debugWebViewMessage" defaultstatus="collapsed">
    /**
     * WebView 诊断与排查问题的方法和技巧，日志输出格式化
     *
     * [相关博客](https://droidyue.com/blog/2019/10/20/how-to-diagnose-webview-in-android/)
     */
    @Suppress("LongMethod", "LongParameterList")
    private fun debugWebViewMessage(
        methodName: String,
        webView: WebView,
        request: WebResourceRequest? = null,
        threatType: Int? = null,
        error: WebResourceErrorCompat? = null,
        errorResponse: WebResourceResponse? = null,
        sslError: SslError? = null,
    ) {
        if (!debugLog) {
            return
        }
        Log.d(TAG, buildString {
            append("debugMessage: ")
            append(methodName)
            append(" webView.info: url=")
            append(webView.url)
            append(", ")
            append(webView.originalUrl)
            request?.run {
                append(", request={ url=")
                append(url)
                append(", isForMainFrame=")
                append(isForMainFrame)
                append(", hasGesture=")
                append(hasGesture())
                append(", method=")
                append(method)
                append(", headers=")
                append(requestHeaders)
                if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_REQUEST_IS_REDIRECT)) {
                    append(", isRedirect=")
                    append(WebResourceRequestCompat.isRedirect(this))
                }
                append('}')
            }
            threatType?.run {
                append(", threatType=")
                append(threatType)
            }
            error?.run {
                append(", error={ errorCode=")
                if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_ERROR_GET_CODE)) {
                    append(errorCode)
                } else {
                    append("'notSupport'")
                }
                append(", description=")
                if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_ERROR_GET_DESCRIPTION)) {
                    append(description)
                } else {
                    append("'notSupport'")
                }
                append('}')
            }
            errorResponse?.run {
                append(", errorResponse={ statusCode=")
                append(statusCode)
                append(", reason=")
                append(reasonPhrase)
                append(", responseHeaders=")
                append(responseHeaders)
                append('}')
            }
            sslError?.run {
                append(", sslError=")
                append(sslError)
            }
        })
    }
    //</editor-fold>

    companion object {
        private const val TAG = "KW_BaseLogWebViewClient"

        // release 下由于混淆规则，该参数会被认为永远为false，即不会输出debug日志
        var debugLog = false
    }
}
