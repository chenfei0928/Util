package io.github.chenfei0928.repository.storage

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract

/**
 * 文件导入处理，返回文件的uri，可能需要使用[Context.getContentResolver]来读取
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-19 12:49
 */
open class FileImportUriFragment : BasePermissionFileImportFragment<Uri>(
    requestPermission = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    )
) {
    // 5.0以上才支持SAF框架
    override val permissionMaxSdkVersion: Int = Build.VERSION_CODES.LOLLIPOP
    override val permissionName: String
        get() = "存储卡"

    private val fileUriImportLauncher = registerForActivityResult(FileUriImportContract()) {
        removeSelf(it)
    }

    /**
     * 忽略回调、权限检查，直接启动文件选择
     */
    final override fun launchFileChooseImpl() {
        val mimeType = arguments?.getString(KEY_MIME_TYPE)
        if (mimeType.isNullOrBlank()) {
            post { removeSelf(null) }
            return
        }
        fileUriImportLauncher.launch(mimeType)
    }

    companion object {
        const val KEY_MIME_TYPE = "mimeType"

        fun newInstance(mimeType: String) = FileImportUriFragment().apply {
            arguments = createArg(mimeType)
        }

        fun createArg(mimeType: String) = Bundle().apply {
            putString(KEY_MIME_TYPE, mimeType)
        }
    }

    private class FileUriImportContract : ActivityResultContract<String, Uri?>() {

        override fun createIntent(context: Context, mimeType: String?): Intent {
            val intent = Intent()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = mimeType
            } else {
                intent.action = Intent.ACTION_OPEN_DOCUMENT
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = mimeType
            }
            return Intent.createChooser(intent, "选择要导入的文件")
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return intent?.data
        }
    }
}
