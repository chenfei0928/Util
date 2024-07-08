package com.github.lzyzsd.jsbridge

import android.content.Context
import android.webkit.WebView
import android.widget.ProgressBar
import io.github.chenfei0928.util.Log
import io.github.chenfei0928.webkit.BaseWebViewClient
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Created by bruce on 10/28/15.
 */
abstract class SupportBridgeWebViewClient(
    context: Context,
    progressBar: ProgressBar? = null
) : BaseWebViewClient(context, progressBar) {

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        val webView = view as? BridgeWebView
            ?: return super.shouldOverrideUrlLoading(view, url)

        var urlDecoded = url
        try {
            urlDecoded = URLDecoder.decode(urlDecoded, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            Log.i(TAG, "shouldOverrideUrlLoading: $url", e)
        }

        return if (urlDecoded.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
            Log.i(TAG, "shouldOverrideUrlLoading: return $urlDecoded")
            webView.handlerReturnData(urlDecoded)
            true
        } else if (urlDecoded.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            Log.i(TAG, "shouldOverrideUrlLoading: load jsBridge")
            webView.flushMessageQueue()
            true
        } else {
            super.shouldOverrideUrlLoading(view, urlDecoded)
        }
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        val webView = view as? BridgeWebView
            ?: return

        BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.toLoadJs)

        if (webView.startupMessage != null) {
            for (m in webView.startupMessage) {
                webView.dispatchMessage(m)
            }
            webView.startupMessage = null
        }
    }

    companion object {
        private const val TAG = "KW_SBridgeWebViewC"
    }
}
