package io.github.chenfei0928.repository.storage.picture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import io.github.chenfei0928.app.result.registerAllActivityResultLauncher
import io.github.chenfei0928.app.result.registerForActivityResultDelegate
import io.github.chenfei0928.content.FileProviderUtil
import io.github.chenfei0928.io.FileUtil
import io.github.chenfei0928.util.R

/**
 * 提供图片选择、并裁剪的导入
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-19 17:27
 */
internal class PictureCropImportV19Fragment : AbsCropImportV19Fragment() {

    private val pictureSourceLauncher by registerForActivityResultDelegate {
        registerForActivityResult(PictureSourceContract(this::requireContext)) {
            if (it != null) {
                // 获取到了图片，进行裁剪
                cropImage(it)
            } else {
                removeSelf(null)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerAllActivityResultLauncher()
    }

    override fun launchFileChoose() {
        pictureSourceLauncher.launch(null)
    }

    private class PictureSourceContract(
        private val context: () -> Context
    ) : ActivityResultContract<Unit?, Uri?>() {
        // 将获取到的文件复制到缓存目录，以修复部分魅族9.0的设备对转发过去的uri无法授予权限的bug
        private val needCopyFile = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && Build.BRAND.equals("Meizu", true)

        override fun createIntent(context: Context, input: Unit?): Intent {
            val intent = Intent()
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            return Intent.createChooser(
                intent, context.getString(R.string.cf0928util_toast_extStrong_chooseImportFile)
            )
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            val uri = if (resultCode == Activity.RESULT_OK) {
                intent?.data
            } else {
                null
            } ?: return null
            // 修复部分设备在选择文件，并将选择的文件转发给图片裁剪器后，图片裁剪器获取不到权限的问题
            return if (needCopyFile) {
                val context = context()
                // 将获取到的文件复制到缓存目录，以修复部分魅族9.0的设备对转发过去的uri无法授予权限的bug
                val tmpFile = obtainPictureFile(context)
                FileUtil.copyUriToDestFile(context, uri, tmpFile)
                // 将导入的临时文件返回
                FileProviderUtil.createUriFromFile(context, tmpFile)
            } else {
                // 获取到了图片，进行裁剪
                uri
            }
        }
    }
}
