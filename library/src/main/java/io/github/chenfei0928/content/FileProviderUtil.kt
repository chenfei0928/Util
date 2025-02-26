package io.github.chenfei0928.content

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.collection.ArrayMap
import androidx.core.content.FileProvider
import io.github.chenfei0928.collection.getContainOrPut
import io.github.chenfei0928.io.FileUtil
import java.io.File

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-19 13:32
 */
object FileProviderUtil {
    var defaultFileProviderClass: Class<out FileProvider> = FileProvider::class.java
    private val schemeCache = ArrayMap<Class<out FileProvider>, String?>()

    /**
     * 获取指定[FileProvider]在Manifest文件中注册的的authority信息
     */
    fun findManifestFileProviderScheme(
        context: Context, clazz: Class<out FileProvider> = defaultFileProviderClass
    ): String = schemeCache.getContainOrPut(clazz) {
        val fileProviderClassName = clazz.name
        return@getContainOrPut context.packageManager.getPackageInfo(
            context.packageName, PackageManager.GET_PROVIDERS
        ).providers?.find {
            it.name == fileProviderClassName
        }?.authority
    } ?: throw IllegalArgumentException(
        "未找到 AndroidManifest.xml 组件注册：$defaultFileProviderClass"
    )

    /**
     * 获取指定文件在指定[FileProvider]中的uri
     */
    fun createUriFromFile(
        context: Context, file: File, clazz: Class<out FileProvider> = defaultFileProviderClass
    ): Uri {
        val fileProviderScheme = findManifestFileProviderScheme(context, clazz)
        return FileProvider.getUriForFile(context, fileProviderScheme, file)
    }

    /**
     * 创建指定Uri的分享[Intent]
     */
    fun createShareIntent(label: String, fileUri: Uri): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        // MIME 类型： http://www.w3school.com.cn/media/media_mimeref.asp
        intent.type = MimeTypeMap
            .getSingleton()
            .getMimeTypeFromExtension(FileUtil.getFileExtensionFromUrl(fileUri.toString()))
        intent.putExtra(Intent.EXTRA_SUBJECT, label)
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        return intent
    }
}
