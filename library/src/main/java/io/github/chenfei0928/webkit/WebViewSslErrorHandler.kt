package io.github.chenfei0928.webkit

import android.app.Dialog
import android.content.Context
import android.net.http.SslError
import android.webkit.SslErrorHandler
import androidx.appcompat.app.AlertDialog
import androidx.collection.ArrayMap
import androidx.core.net.toUri
import io.github.chenfei0928.util.R

/**
 * WebView SSL证书验证错误处理器
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-08-25 17:45
 */
internal class WebViewSslErrorHandler(
    private val context: Context
) {
    // 网站域名到其是否忽略证书校验信息和处理器的映射
    private val sslErrorHandlerOpinion = ArrayMap<String?, Opinion>()

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
        private var isAllowed: Boolean? = null

        @Volatile
        private var listeners: MutableSet<SslErrorHandler>? = mutableSetOf()

        private var dialog: Dialog? = AlertDialog
            .Builder(context)
            .setMessage(R.string.cf0928util_sslError_message)
            .setPositiveButton(R.string.cf0928util_sslError_ignoreAngGoOn) { _, _ ->
                onProcessResult(true)
            }
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
            when (val allowed = isAllowed) {
                null -> {
                    listeners?.add(handler)
                    dialog?.show()
                }
                allowed -> handler.proceed()
                else -> handler.cancel()
            }
        }
    }
}
