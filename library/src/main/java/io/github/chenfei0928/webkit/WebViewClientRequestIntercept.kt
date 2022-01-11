package io.github.chenfei0928.webkit

import android.content.res.AssetManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import io.github.chenfei0928.util.Log
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-15 16:37
 */
class WebViewClientRequestIntercept {
    private val TAG = "KW_WebViewRequestInter"

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun shouldInterceptRequest(view: WebView, request: WebResourceRequest?): WebResourceResponse? {
        request ?: return null
        return shouldInterceptRequest(view, request.method, request.url, request.requestHeaders)
    }

    fun shouldInterceptRequest(view: WebView, url: String?): WebResourceResponse? {
        return shouldInterceptRequest(view, "GET", url?.toUri(), emptyMap())
    }

    private fun shouldInterceptRequest(
        view: WebView,
        method: String,
        uri: Uri?,
        requestHeaders: Map<String, String>
    ): WebResourceResponse? {
        uri ?: return null
        if (uri.host != "localhost") {
            return null
        }
        return when (uri.path) {
            "resources" -> {
                loadResource(view.resources, uri.getQueryParameter("a"))
            }
            "assets" -> {
                loadAssets(view.context.assets, uri.getQueryParameter("a"))
            }
            else -> null
        }
    }

    private fun loadAssets(assets: AssetManager, path: String?): WebResourceResponse? {
        path ?: return null
        return try {
            createWebResourceResponse(assets.open(path))
        } catch (e: Throwable) {
            Log.d(TAG, "loadAssets: ", e)
            null
        }
    }

    private fun loadResource(resources: Resources, path: String?): WebResourceResponse? {
        path ?: return null
        try {
            // 解析参数
            val packageName = path.substring(0, path.indexOf(':'))
            val path = path.substring(path.indexOf(':'))
            val type = path.substring(0, path.indexOf('.'))
            val name = path.substring(path.indexOf('.'))
            val id = resources.getIdentifier(name, type, packageName)
            // 打开原始资源输入流
            return when (type) {
                "dimen" -> {
                    val value = resources.getDimensionPixelSize(id)
                    createWebResourceResponse(value.toString())
                }
                "bool" -> {
                    val value = resources.getBoolean(id)
                    createWebResourceResponse(value.toString())
                }
                "string" -> {
                    val value = resources.getString(id)
                    createWebResourceResponse(value.toString())
                }
                "color" -> {
                    val value = resources.getColor(id)
                    createWebResourceResponse(Integer.toHexString(value))
                }
                "integer" -> {
                    val value = resources.getInteger(id)
                    createWebResourceResponse(value.toString())
                }
                "drawable", "mipmap" -> {
                    WebResourceResponse("image/webp", "", resources.openRawResource(id))
                }
                else -> {
                    createWebResourceResponse(resources.openRawResource(id))
                }
            }
        } catch (e: Throwable) {
            Log.d(TAG, "loadResource: ", e)
            return null
        }
    }

    private fun createWebResourceResponse(inputStream: String): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            Charsets.UTF_8.name(),
            ByteArrayInputStream(inputStream.toByteArray())
        )
    }

    private fun createWebResourceResponse(inputStream: InputStream): WebResourceResponse {
        return WebResourceResponse("application/octet-stream", "", inputStream)
    }
}
