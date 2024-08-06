package io.github.chenfei0928.webkit

import android.webkit.ConsoleMessage
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.Size
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebResourceRequestCompat
import androidx.webkit.WebViewFeature
import io.github.chenfei0928.util.Log

/**
 * WebView 诊断与排查问题的方法和技巧，日志输出格式化
 *
 * [相关博客](https://droidyue.com/blog/2019/10/20/how-to-diagnose-webview-in-android/)
 */
fun WebView.toSimpleString(): String {
    return "url=${url}, $originalUrl"
}

fun WebResourceRequest.toSimpleString(): String {
    @Suppress("MaxLineLength")
    var simpleString =
        "uri=${url}, isForMainFrame=${isForMainFrame}, hasGesture=${hasGesture()}, method=${method}, headers=${requestHeaders}"

    if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_REQUEST_IS_REDIRECT)) {
        simpleString += "isRedirect=${WebResourceRequestCompat.isRedirect(this)}"
    }
    return simpleString
}

fun WebResourceErrorCompat.toSimpleString(): String {
    val errorCode =
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_ERROR_GET_CODE)) {
            errorCode
        } else {
            -1
        }
    val description =
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_ERROR_GET_DESCRIPTION)) {
            description
        } else {
            "-"
        }
    return "errorCode=${errorCode}, description=${description}"
}

fun WebResourceResponse.toSimpleString(): String {
    return "statusCode=${statusCode}, reason=${reasonPhrase}, responseHeaders=${responseHeaders}"
}

fun ConsoleMessage.toSimpleString(): String {
    return "${message()}\n at ${sourceId()}:${lineNumber()}"
}

internal fun debugWebViewMessage(
    @Size(max = 23) tag: String,
    methodName: String,
    webView: WebView,
    @Size(multiple = 2) params: Array<String?>
) {
    if (!BaseWebViewClient.debugLog) {
        return
    }
    Log.d(tag, buildString {
        append("debugMessage: ")
        append(methodName)
        append(" webView.info")
        append(webView.toSimpleString())
        for (index in params.indices step 2) {
            append(", ")
            append(params[index])
            append('+')
            append(params[index + 1])
        }
    })
}
