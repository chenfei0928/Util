package com.chenfei.base.webview

import android.graphics.Bitmap
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar
import com.github.lzyzsd.jsbridge.BridgeWebView

/**
 * User: ChenFei(chenfei0928@gmail.com)
 * Date: 2019-05-21
 * Time: 16:54
 *
 * @author MrFeng
 */
open class BaseLibWebViewClient(
        private val mProgress: ProgressBar
) : BaseLogWebViewClient() {
    private val TAG = "KW_BaseLibWebViewClient"
    private var bridgeWebView: BridgeWebView? = null

    /**
     * 网页开始加载时触发
     */
    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        mProgress.visibility = View.VISIBLE
        super.onPageStarted(view, url, favicon)
    }

    /**
     * 网页加载结束时触发
     */
    override fun onPageFinished(view: WebView, url: String) {
        mProgress.visibility = View.GONE
        super.onPageFinished(view, url)
    }
}
