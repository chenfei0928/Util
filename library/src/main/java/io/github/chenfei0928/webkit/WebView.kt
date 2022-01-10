package io.github.chenfei0928.webkit

import android.os.Build
import android.webkit.ConsoleMessage
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebViewFeature

/**
 * WebView 诊断与排查问题的方法和技巧，日志输出格式化
 *
 * [相关博客](https://droidyue.com/blog/2019/10/20/how-to-diagnose-webview-in-android/)
 */
fun WebView.toSimpleString(): String {
    return "url=${url}; originalUrl=${originalUrl}"
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun WebResourceRequest.toSimpleString(): String {
    var simpleString =
        "uri=${url}; isForMainFrame=${isForMainFrame}; hasGesture=${hasGesture()}; method=${method}; headers=${requestHeaders}"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        simpleString += "isRedirect=${isRedirect}; "
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
    return "errorCode=${errorCode}; description=${description}"
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun WebResourceResponse.toSimpleString(): String {
    return "statusCode=${statusCode}; reason=${reasonPhrase}; responseHeaders=${responseHeaders}"
}

fun ConsoleMessage.toSimpleString(): String {
    return "${message()}\nat ${sourceId()}:${lineNumber()}"
}
