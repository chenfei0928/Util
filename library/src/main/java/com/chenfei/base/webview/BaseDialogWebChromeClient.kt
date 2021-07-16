package com.chenfei.base.webview

import android.content.DialogInterface
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import com.chenfei.app.EditorDialogBuilder
import com.chenfei.lib_base.R
import com.chenfei.util.Log

/**
 * 用于基本弹框的client
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-15 11:51
 */
open class BaseDialogWebChromeClient : BaseLogWebChromeClient() {

    /**
     * 当加载的网页弹出js警告对话框时触发，注意返回值true表示以WebChromeClient处理为基准
     */
    override fun onJsAlert(view: WebView, url: String, message: String,
                           result: JsResult): Boolean {
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
    override fun onJsConfirm(view: WebView, url: String, message: String,
                             result: JsResult): Boolean {
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
    override fun onJsPrompt(view: WebView, url: String, message: String,
                            defaultValue: String, result: JsPromptResult): Boolean {
        EditorDialogBuilder(view.context)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(message)
                .setHint(defaultValue)
                .setSingleLine()
                .setPositiveButton(android.R.string.ok) { _, ed, _ ->
                    result.confirm(if (ed.length() > 0) ed.text.toString() else defaultValue)
                    Unit
                }
                .setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener { _, _ ->
                    result.cancel()
                })
                .setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, event ->
                    // 屏蔽keycode等于84之类的按键，避免按键后导致对话框消息而页面无法再弹出对话框的问题
                    Log.v("onJsPrompt", "keyCode==" + keyCode + "event=" + event)
                    true
                })
                .setCancelable(false)
                .show()
        return true
    }
}
