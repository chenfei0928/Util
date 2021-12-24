package io.github.chenfei0928.base.webview

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.os.Message
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.ProgressBar
import io.github.chenfei0928.base.fragment.FragmentHost
import io.github.chenfei0928.util.Log

/**
 * @author MrFeng
 */
open class BaseLibWebChromeClient(
    private var host: FragmentHost? = null,
    private val mClientCallback: WebViewClientCallback,
    private val mProgress: ProgressBar?
) : BaseDialogWebChromeClient() {

    /**
     * 当加载网页进度发生改变时触发
     */
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        mProgress?.progress = newProgress
        super.onProgressChanged(view, newProgress)
    }

    /**
     * 请求焦点
     */
    override fun onRequestFocus(view: WebView) {
        super.onRequestFocus(view)
        view.requestFocus()
    }

    /**
     * 当窗口创建时触发
     */
    override fun onCreateWindow(
        view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?
    ): Boolean {
        Log.d(TAG, "onCreateWindow")
        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
    }

    /**
     * 当WebView窗口关闭时触发
     */
    override fun onCloseWindow(window: WebView) {
        super.onCloseWindow(window)
        Log.d(TAG, "onCloseWindow")
        mClientCallback.onWebViewClose()
    }

    /**
     * 打开文件选择器，此api为android 5.0（API 21）加入
     * 低于该版本系统中每个API-Level系统的api都不一致，4.4.4则没有相关接口提供
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        val fragmentHost = host
        // 有fragmentHost，启动文件选择器
        return if (fragmentHost != null) {
            val fragment = WebViewFileChooseLighterFragment().apply {
                this.filePathCallback = filePathCallback
                this.fileChooserParams = fileChooserParams
            }
            fragmentHost
                .getSupportFragmentManager()
                .beginTransaction()
                .add(fragment, "WebViewFileChooseLighterFragment")
                .commitAllowingStateLoss()
            true
        } else {
            super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }
    }

    companion object {
        private const val TAG = "KW_WebChromeClient"
    }
}
