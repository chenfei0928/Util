package io.github.chenfei0928.base.webview

import android.webkit.ConsoleMessage
import androidx.annotation.Size
import io.github.chenfei0928.util.Log

/**
 * 用于输出日志的Client
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-15 11:48
 */
open class BaseLogWebChromeClient : android.webkit.WebChromeClient() {

    override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
        // 不需要调用super方法
        debugMessage(
            "onConsoleMessage", ConsoleMessage.MessageLevel.DEBUG, arrayOf(
                "message", message, "lineNumber", lineNumber.toString(), "sourceID", sourceID
            )
        )
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        debugMessage(
            "onConsoleMessage", consoleMessage.messageLevel(), arrayOf(
                "message", consoleMessage.toSimpleString()
            )
        )
        // 返回true，不再需要webview内部处理
        return true
    }

    private fun debugMessage(
        methodName: String,
        level: ConsoleMessage.MessageLevel,
        @Size(multiple = 2, min = 0) params: Array<String?>
    ) {
        if (!debugLog) {
            return
        }
        val sb = StringBuilder()
            .append("debugMessage: ")
            .append(methodName)
        for (i in params.indices step 2) {
            sb
                .append(params[i])
                .append('=')
                .append(params[i + 1])
        }
        when (level) {
            ConsoleMessage.MessageLevel.TIP -> Log.v(TAG, sb.toString())
            ConsoleMessage.MessageLevel.LOG -> Log.i(TAG, sb.toString())
            ConsoleMessage.MessageLevel.WARNING -> Log.w(TAG, sb.toString())
            ConsoleMessage.MessageLevel.ERROR -> Log.e(TAG, sb.toString())
            ConsoleMessage.MessageLevel.DEBUG -> Log.d(TAG, sb.toString())
        }
    }

    companion object {
        private const val TAG = "KW_LogWebChromeClient"
        var debugLog = false
    }
}
