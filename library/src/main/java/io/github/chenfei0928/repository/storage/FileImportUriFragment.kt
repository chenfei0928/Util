package io.github.chenfei0928.repository.storage

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.UiThread
import io.github.chenfei0928.app.result.registerAllActivityResultLauncher
import io.github.chenfei0928.app.result.registerForActivityResultDelegate
import io.github.chenfei0928.os.safeHandler

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

    private val fileUriImportLauncher by registerForActivityResultDelegate {
        registerForActivityResult(FileUriImportContract()) { removeSelf(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerAllActivityResultLauncher()
    }

    /**
     * 忽略回调、权限检查，直接启动文件选择
     */
    @UiThread
    final override fun launchFileChooseImpl() {
        val mimeType = arguments?.getString(KEY_MIME_TYPE)
        if (mimeType.isNullOrBlank()) {
            safeHandler.post { removeSelf(null) }
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

        override fun createIntent(context: Context, input: String): Intent {
            val intent = Intent().apply {
                action = Intent.ACTION_OPEN_DOCUMENT
                addCategory(Intent.CATEGORY_OPENABLE)
                type = input
            }
            return Intent.createChooser(intent, "选择要导入的文件")
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return intent?.data
        }
    }
}
