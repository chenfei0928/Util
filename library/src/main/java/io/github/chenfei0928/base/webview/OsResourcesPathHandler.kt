package io.github.chenfei0928.base.webview

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.webkit.WebResourceResponse
import androidx.core.content.ContextCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.internal.AssetHelper
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.GZIPInputStream

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-15 16:37
 */
internal class OsResourcesPathHandler(
    private val context: Context
) : WebViewAssetLoader.PathHandler {
    private val resources: Resources = context.resources

    override fun handle(path: String): WebResourceResponse {
        try {
            val mimeType = AssetHelper.guessMimeType(path)
            return openResourceAsResponse(path, mimeType)
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Resource not found from the path: $path", e)
        } catch (e: IOException) {
            Log.e(TAG, "Error opening resource from the path: $path", e)
        }
        return WebResourceResponse(null, null, null)
    }

    @Throws(Resources.NotFoundException::class, IOException::class)
    private fun openResourceAsResponse(path: String, mimeType: String): WebResourceResponse {
        val path = if (path.length > 1 && path[0] == '/') {
            path.substring(1)
        } else {
            path
        }
        // The path must be of the form "resource_type/resource_name.ext".
        val pathSegments = path
            .split("/")
            .dropLastWhile { it.isEmpty() }
        require(pathSegments.size == 3) { "Incorrect resource path: $path" }
        val resourcePackage = pathSegments[0]
        val resourceType = pathSegments[1]
        var resourceName = pathSegments[2]
        // Drop the file extension.
        val dotIndex = resourceName.lastIndexOf('.')
        if (dotIndex != -1) {
            resourceName = resourceName.substring(0, dotIndex)
        }
        val fieldId = resources.getIdentifier(resourceName, resourceType, resourcePackage)
        // 打开原始资源输入流
        return when (resourceType) {
            "dimen" -> {
                val value = resources.getDimensionPixelSize(fieldId)
                createWebResourceResponse(value.toString())
            }
            "bool" -> {
                val value = resources.getBoolean(fieldId)
                createWebResourceResponse(value.toString())
            }
            "string" -> {
                val value = resources.getString(fieldId)
                createWebResourceResponse(value)
            }
            "color" -> {
                val value = ContextCompat.getColor(context, fieldId)
                createWebResourceResponse(Integer.toHexString(value))
            }
            "integer" -> {
                val value = resources.getInteger(fieldId)
                createWebResourceResponse(value.toString())
            }
            "drawable", "mipmap" -> {
                WebResourceResponse(mimeType, null, resources.openRawResource(fieldId))
            }
            else -> {
                val stream = resources.openRawResource(fieldId)
                val inputStream = if (path.endsWith(".svgz")) GZIPInputStream(stream) else stream
                WebResourceResponse(mimeType, null, inputStream)
            }
        }
    }

    private fun createWebResourceResponse(inputStream: String): WebResourceResponse {
        return WebResourceResponse(
            "text/plain", Charsets.UTF_8.name(), ByteArrayInputStream(inputStream.toByteArray())
        )
    }

    companion object {
        private const val TAG = "KW_OsResourcesPathH"
    }
}
