package io.github.chenfei0928.webkit

import android.net.Uri
import android.os.Build
import android.webkit.ConsoleMessage
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebResourceRequestCompat
import androidx.webkit.WebViewFeature
import io.github.chenfei0928.util.Log

private const val TAG = "KW_WebView"

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

    if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_REQUEST_IS_REDIRECT)) {
        simpleString += "isRedirect=${WebResourceRequestCompat.isRedirect(this)}; "
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

internal fun debugWebViewMessage(
    methodName: String, vararg params: Pair<String, String?>
) = debugWebViewMessage(methodName, ConsoleMessage.MessageLevel.DEBUG, params = params)

internal fun debugWebViewMessage(
    methodName: String,
    level: ConsoleMessage.MessageLevel = ConsoleMessage.MessageLevel.DEBUG,
    vararg params: Pair<String, String?>
) {
    if (!BaseWebViewClient.debugLog) {
        return
    }
    val msg = params.joinTo(
        StringBuilder()
            .append("debugMessage: ")
            .append(methodName)
    ) {
        it.first + "+" + it.second
    }.toString()
    when (level) {
        ConsoleMessage.MessageLevel.TIP -> Log.v(TAG, msg)
        ConsoleMessage.MessageLevel.LOG -> Log.i(TAG, msg)
        ConsoleMessage.MessageLevel.WARNING -> Log.w(TAG, msg)
        ConsoleMessage.MessageLevel.ERROR -> Log.e(TAG, msg)
        ConsoleMessage.MessageLevel.DEBUG -> Log.d(TAG, msg)
    }
}

sealed interface WebResourceRequestSupport {
    val url: Uri
    val isForMainFrame: Boolean
    val isRedirect: Boolean
    val hasGesture: Boolean
    val method: String
    val requestHeaders: Map<String, String>
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class WebResourceRequestSupportV21(
    private val target: WebResourceRequest
) : WebResourceRequestSupport {
    override val url: Uri
        get() = target.url
    override val isForMainFrame: Boolean
        get() = target.isForMainFrame
    override val isRedirect: Boolean
        get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_REQUEST_IS_REDIRECT)) {
            WebResourceRequestCompat.isRedirect(target)
        } else {
            false
        }
    override val hasGesture: Boolean
        get() = target.hasGesture()
    override val method: String
        get() = target.method
    override val requestHeaders: Map<String, String>
        get() = target.requestHeaders
}

internal class WebResourceRequestSupportBase(
    url: String
) : WebResourceRequestSupport {
    override val url: Uri = Uri.parse(url)
    override val isForMainFrame: Boolean = false
    override val isRedirect: Boolean = false
    override val hasGesture: Boolean = false
    override val method: String = "GET"
    override val requestHeaders: Map<String, String> = emptyMap()
}
