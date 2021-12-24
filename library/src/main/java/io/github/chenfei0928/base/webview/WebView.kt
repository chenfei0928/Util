package io.github.chenfei0928.base.webview

import android.os.Build
import android.webkit.*
import androidx.annotation.RequiresApi

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

fun WebResourceError.toSimpleString(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        "errorCode=${errorCode}; description=${description}"
    } else {
        ""
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun WebResourceResponse.toSimpleString(): String {
    return "statusCode=${statusCode}; reason=${reasonPhrase}; responseHeaders=${responseHeaders}"
}

fun ConsoleMessage.toSimpleString(): String {
    return "${message()}\nat ${sourceId()}:${lineNumber()}"
}
