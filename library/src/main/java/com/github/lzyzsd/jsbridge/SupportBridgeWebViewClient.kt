package com.github.lzyzsd.jsbridge

import android.webkit.WebView
import androidx.webkit.WebViewClientCompat
import io.github.chenfei0928.util.Log
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Created by bruce on 10/28/15.
 */
abstract class SupportBridgeWebViewClient : WebViewClientCompat() {
    private val TAG = "KW_SBridgeWebViewC"

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        val webView = view as? BridgeWebView
            ?: return super.shouldOverrideUrlLoading(view, url)

        var url = url
        try {
            url = URLDecoder.decode(url, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
            Log.i(TAG, "shouldOverrideUrlLoading: return $url")
            webView.handlerReturnData(url)
            return true
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            Log.i(TAG, "shouldOverrideUrlLoading: load jsBridge")
            webView.flushMessageQueue()
            return true
        } else {
            return super.shouldOverrideUrlLoading(view, url)
        }
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        val webView = view as? BridgeWebView
            ?: return

        if (BridgeWebView.toLoadJs != null) {
            BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.toLoadJs)
        }

        //
        if (webView.startupMessage != null) {
            for (m in webView.startupMessage) {
                webView.dispatchMessage(m)
            }
            webView.startupMessage = null
        }
    }
}
