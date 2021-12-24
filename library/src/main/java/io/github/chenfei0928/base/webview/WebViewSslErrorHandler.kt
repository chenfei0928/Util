package io.github.chenfei0928.base.webview

import android.app.Dialog
import android.content.Context
import android.net.http.SslError
import android.webkit.SslErrorHandler
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri

/**
 * WebView SSL证书验证错误处理器
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-08-25 17:45
 */
internal class WebViewSslErrorHandler(
    private val context: Context
) {
    private val sslErrorHandlerOpinion = mutableMapOf<String?, Opinion>()

    fun emit(handler: SslErrorHandler, error: SslError?) {
        sslErrorHandlerOpinion
            .getOrPut(error?.url?.toUri()?.host) {
                Opinion(context)
            }
            .process(handler)
    }

    private class Opinion(
        context: Context
    ) {
        @Volatile
        var isAllowed: Boolean? = null

        @Volatile
        var listeners: MutableSet<SslErrorHandler>? = mutableSetOf()

        private var dialog: Dialog? = AlertDialog
            .Builder(context)
            .setMessage("ssl证书验证失败")
            .setPositiveButton("继续") { _, _ -> onProcessResult(true) }
            .setNegativeButton(android.R.string.cancel) { _, _ -> onProcessResult(false) }
            .setOnCancelListener { onProcessResult(false) }
            .create()

        @Synchronized
        private fun onProcessResult(isAllowed: Boolean) {
            this.isAllowed = true
            listeners?.forEach {
                if (isAllowed) {
                    it.proceed()
                } else {
                    it.cancel()
                }
            }
            listeners = null
            dialog = null
        }

        @Synchronized
        fun process(handler: SslErrorHandler) {
            val allowed = isAllowed
            when {
                allowed == null -> {
                    listeners?.add(handler)
                    dialog?.show()
                }
                allowed -> {
                    handler.proceed()
                }
                else -> {
                    handler.cancel()
                }
            }
        }
    }
}
