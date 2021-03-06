package com.chenfei.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.chenfei.content.YikeFileProvider
import java.io.File

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-19 13:32
 */
class IntentKt {
    companion object {
        fun findManifestFileProviderScheme(context: Context): String? =
            findManifestFileProviderScheme(context, YikeFileProvider::class.java)

        fun <F : FileProvider> findManifestFileProviderScheme(
            context: Context, clazz: Class<F>
        ): String? {
            val fileProviderClassName = clazz::class.java.name
            return context.packageManager.getPackageInfo(
                context.packageName, PackageManager.GET_PROVIDERS
            ).providers.find {
                it.name == fileProviderClassName
            }?.authority
        }

        fun createUriFromFile(context: Context, file: File): Uri? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val fileProviderScheme = findManifestFileProviderScheme(context) ?: ""
                FileProvider.getUriForFile(context, fileProviderScheme, file)
            } else {
                Uri.fromFile(file)
            }
        }

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
}
