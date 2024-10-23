package io.github.chenfei0928.webkit

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.webkit.SafeBrowsingResponseCompat
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import io.github.chenfei0928.util.Log
import java.io.File

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-14 14:05
 */
open class BaseWebViewClient(
    private val context: Context,
    private val progressBar: ProgressBar? = null
) : WebViewClientCompat() {

    //<editor-fold defaultstate="collapsed" desc="日志">
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
            TAG, "onReceivedError", view, arrayOf(
                "request", request.toSimpleString(),
                "error", error.toSimpleString(),
            )
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
            TAG, "onSafeBrowsingHit", view, arrayOf(
                "request", request.toSimpleString(),
                "threatType", threatType.toString(),
            )
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
            TAG, "onReceivedHttpError", view, arrayOf(
                "request", request.toSimpleString(),
                "errorResponse", errorResponse.toSimpleString(),
            )
        )
    }

    /**
     * 接收到ssl证书错误的回调
     *
     * Added in API level 8
     */
    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError?) {
        super.onReceivedSslError(view, handler, error)
        // 如果debug情况下忽略了证书错误，直接允许
        @SuppressLint("WebViewClientOnReceivedSslError")
        if (ignoreSslError) {
            handler.proceed()
            return
        }
        // 弹出警告
        debugWebViewMessage(
            TAG, "onReceivedSslError", view, arrayOf(
                "error", error?.toString(),
            )
        )
        sslErrorHandler.emit(handler, error)
    }

    private val sslErrorHandler by lazy {
        WebViewSslErrorHandler(context)
    }

    /**
     * 渲染进程丢失时的回调
     * [ApiDocs](https://developer.android.com/reference/android/webkit/WebViewClient.html#onRenderProcessGone(android.webkit.WebView,%20android.webkit.RenderProcessGoneDetail))
     *
     * Added in API level 26
     */
    @TargetApi(Build.VERSION_CODES.O)
    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        return if (detail.didCrash()) {

            // Renderer crashed because of an internal error, such as a memory
            // access violation.
            if (debugLog) {
                Log.e(TAG, "The WebView rendering process crashed!")
            }

            // In this example, the app itself crashes after detecting that the
            // renderer crashed. If you choose to handle the crash more gracefully
            // and allow your app to continue executing, you should 1) destroy the
            // current WebView instance, 2) specify logic for how the app can
            // continue executing, and 3) return "true" instead.
            false
        } else {
            // Renderer was killed because the system ran out of memory.
            // The app can recover gracefully by creating a new WebView instance
            // in the foreground.
            if (debugLog) {
                Log.e(TAG, run {
                    "System killed the WebView rendering process to reclaim memory. Recreating..."
                })
            }

            WebViewSettingsUtil.onDestroy(view)

            // By this point, the instance variable "mWebView" is guaranteed
            // to be null, so it's safe to reinitialize it.

            true // The app continues executing.
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (debugLog) {
            Log.v(TAG, "shouldOverrideUrlLoading: ${request.toSimpleString()}")
        }
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (debugLog) {
            Log.v(TAG, "onPageStarted: $url")
        }
        progressBar?.visibility = View.VISIBLE
        progressBar?.progress = 0
    }

    override fun onPageFinished(view: WebView, url: String) {
        progressBar?.visibility = View.GONE
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
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="请求拦截返回">
    var assetLoader: WebViewAssetLoader = WebViewAssetLoader.Builder()
        // https://appassets.androidplatform.net/assets/index.html
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        // https://appassets.androidplatform.net/resources/mipmap/ic_launcher.png
        .addPathHandler("/resources/", WebViewAssetLoader.ResourcesPathHandler(context))
        // https://appassets.androidplatform.net/osRes/android/mipmap/sym_def_app_icon.png
        .addPathHandler("/osRes/", OsResourcesPathHandler(context))
        // http://appassets.androidplatform.net/public/ic_launcher.png
        .addPathHandler(
            "/public/", WebViewAssetLoader.InternalStoragePathHandler(
                context, File(context.filesDir, "webViewPublic")
            )
        ).build()
    val interceptRequest: MutableList<(WebResourceRequest) -> WebResourceResponse?> =
        mutableListOf()

    final override fun shouldInterceptRequest(
        view: WebView, request: WebResourceRequest
    ): WebResourceResponse? {
        if (debugLog) {
            Log.i(TAG, "shouldInterceptRequest: ${request.toSimpleString()}")
        }
        return assetLoader.shouldInterceptRequest(request.url)
            ?: interceptRequest.firstNotNullOfOrNull { it(request) }
    }
    //</editor-fold>

    companion object {
        private const val TAG = "KW_BaseWebViewClient"
        var ignoreSslError = false
        var debugLog = false
    }
}
