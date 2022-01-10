package io.github.chenfei0928.repository.storage.picture

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import io.github.chenfei0928.repository.storage.BaseFileImportUriFragment
import java.io.File

/**
 * 提供图片选择、并裁剪的导入（不提供拍照选项）
 * 主要提供图片裁剪的实现，如不需要裁剪，直接使用文件导入即可
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-19 15:10
 */
internal abstract class AbsCropImportBaseFragment : BaseFileImportUriFragment() {
    protected abstract val contract: AbsCropImportContract

    private val cropImportUriLauncher = registerForActivityResult(contract) {
        removeSelf(it)
    }

    /**
     * 忽略回调、权限检查，直接启动文件选择
     */
    override fun launchFileChoose() {
        val headImageSelectUri = Uri.fromFile(
            File(
                requireContext().cacheDir.path, "cache_" + System.currentTimeMillis() + ".png"
            )
        )
        // 将传入的裁图参数传入
        cropImportUriLauncher.launch(
            Params(arguments?.getBundle(CROP_INTENT_PARAM), headImageSelectUri)
        )
    }

    companion object {
        const val CROP_INTENT_PARAM = "cropParam"
    }

    protected data class Params(
        val cropParam: Bundle?, val headImageSelectUri: Uri
    )

    protected abstract class AbsCropImportContract : ActivityResultContract<Params, Uri?>() {

        override fun createIntent(context: Context, input: Params): Intent {
            // 4.4 之下，直接选择并裁切图片即可
            val intent = createPictureSourceIntent(input.headImageSelectUri)
            // 将传入的裁图参数传入
            input.cropParam?.let {
                intent.putExtras(it)
            }
            return intent
        }

        abstract fun createPictureSourceIntent(headImageSelectUri: Uri): Intent

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return intent?.data
        }
    }
}
