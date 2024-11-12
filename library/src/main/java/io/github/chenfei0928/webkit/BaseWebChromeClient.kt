package io.github.chenfei0928.webkit

import android.net.Uri
import android.os.Message
import android.webkit.ConsoleMessage
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import io.github.chenfei0928.app.EditorDialogBuilder
import io.github.chenfei0928.base.fragment.FragmentHost
import io.github.chenfei0928.util.Log
import io.github.chenfei0928.util.R

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-14 14:13
 */
open class BaseWebChromeClient(
    private var host: FragmentHost? = null,
    private val clientCallback: WebViewClientCallback? = null,
    private val progressBar: ProgressBar? = null
) : WebChromeClient() {

    /**
     * 日志输出
     */
    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        if (!BaseLogWebViewClient.debugLog) {
            return true
        }
        val msg = buildString {
            append("debugMessage: onConsoleMessage message=")
            append(consoleMessage.message())
            append("\n at")
            append(consoleMessage.sourceId())
            append(':')
            append(consoleMessage.lineNumber())
        }
        when (consoleMessage.messageLevel()) {
            ConsoleMessage.MessageLevel.TIP -> Log.v(TAG, msg)
            ConsoleMessage.MessageLevel.LOG -> Log.i(TAG, msg)
            ConsoleMessage.MessageLevel.WARNING -> Log.w(TAG, msg)
            ConsoleMessage.MessageLevel.ERROR -> Log.e(TAG, msg)
            ConsoleMessage.MessageLevel.DEBUG -> Log.d(TAG, msg)
            null -> Log.d(TAG, msg)
        }
        // 返回true，不再需要webview内部处理
        return true
    }

    //<editor-fold defaultstate="collapsed" desc="JsDialogUi">
    /**
     * 当加载的网页弹出js警告对话框时触发，注意返回值true表示以WebChromeClient处理为基准
     */
    override fun onJsAlert(
        view: WebView, url: String, message: String,
        result: JsResult
    ): Boolean {
        AlertDialog.Builder(view.context)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
            .setCancelable(false)
            .show()
        return true
    }

    /**
     * 当加载的网页弹出js确认对话框时触发
     */
    override fun onJsConfirm(
        view: WebView, url: String, message: String,
        result: JsResult
    ): Boolean {
        AlertDialog.Builder(view.context)
            .setTitle(R.string.dialog_confirm_title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
            .setNegativeButton(android.R.string.cancel) { _, _ -> result.cancel() }
            .setCancelable(false)
            .show()
        return true
    }

    /**
     * 当加载的网页弹出js提示对话框时触发
     */
    override fun onJsPrompt(
        view: WebView, url: String, message: String,
        defaultValue: String, result: JsPromptResult
    ): Boolean {
        EditorDialogBuilder(view.context)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(message)
            .setHint(defaultValue)
            .setSingleLine()
            .setPositiveButton(android.R.string.ok) { _, ed, _ ->
                result.confirm(if (ed.length() > 0) ed.text.toString() else defaultValue)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                result.cancel()
            }
            .setOnKeyListener { _, keyCode, event ->
                // 屏蔽keycode等于84之类的按键，避免按键后导致对话框消息而页面无法再弹出对话框的问题
                Log.v("onJsPrompt", "keyCode==" + keyCode + "event=" + event)
                true
            }
            .setCancelable(false)
            .show()
        return true
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="用户交互与文件选择">
    /**
     * 当加载网页进度发生改变时触发
     */
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        progressBar?.progress = newProgress
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
        clientCallback?.onWebViewClose()
    }

    /**
     * 打开文件选择器，此api为android 5.0（API 21）加入
     * 低于该版本系统中每个API-Level系统的api都不一致，4.4.4则没有相关接口提供
     */
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
    //</editor-fold>

    companion object {
        private const val TAG = "KW_BaseWebChromeClient"
    }
}
