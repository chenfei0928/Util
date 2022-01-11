package io.github.chenfei0928.repository.storage.picture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import io.github.chenfei0928.content.FileProviderKt
import io.github.chenfei0928.io.FileUtil

/**
 * 提供图片选择、并裁剪的导入
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-19 17:27
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
internal class PictureCropImportV19Fragment : AbsCropImportV19Fragment() {

    private val pictureSourceLauncher =
        registerForActivityResult(PictureSourceContract(this::requireContext)) {
            if (it != null) {
                // 获取到了图片，进行裁剪
                cropImage(it)
            } else {
                removeSelf(null)
            }
        }

    override fun launchFileChoose() {
        pictureSourceLauncher.launch(null)
    }

    private class PictureSourceContract(
        private val context: () -> Context
    ) : ActivityResultContract<Unit?, Uri?>() {
        // 将获取到的文件复制到缓存目录，以修复部分魅族9.0的设备对转发过去的uri无法授予权限的bug
        private val needCopyFile =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && Build.BRAND.equals("Meizu", true)

        override fun createIntent(context: Context, input: Unit?): Intent {
            val intent = Intent()
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            return Intent.createChooser(intent, "选择要导入的文件")
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
                val authority =
                    FileProviderKt.findManifestFileProviderScheme(context) ?: context.packageName
                // 将导入的临时文件返回
                FileProvider.getUriForFile(context, authority, tmpFile)
            } else {
                // 获取到了图片，进行裁剪
                uri
            }
        }
    }
}

/**
 * 提供拍照、并裁剪的导入
 * 由于文件来源是拍照，则需要在结束时撤销文件输出路径的访问权限
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-19 17:27
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
internal class TakePhotoCropImportV19Fragment : AbsCropImportV19Fragment() {
    private lateinit var takePhotoUri: Uri

    private lateinit var pictureSourceLauncher: ActivityResultLauncher<Unit?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePhotoUri =
            savedInstanceState?.getParcelable(KEY_TAKE_PHOTO_URI) ?: obtainPicturePathUri(
                requireContext()
            )
        pictureSourceLauncher = registerForActivityResult(PictureSourceContract(takePhotoUri)) {
            if (it != null) {
                // 获取到了图片，进行裁剪
                cropImage(it)
            } else {
                revokeUriPermission(requireContext(), takePhotoUri)
                removeSelf(null)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_TAKE_PHOTO_URI, takePhotoUri)
    }

    override fun removeSelf(uri: Uri?) {
        // 通知回调时，移除uri的权限授权
        revokeUriPermission(requireContext(), takePhotoUri)
        super.removeSelf(uri)
    }

    override fun launchFileChoose() {
        pictureSourceLauncher.launch(null)
    }

    companion object {
        private const val KEY_TAKE_PHOTO_URI = "takePhotoUri"
    }

    private class PictureSourceContract(
        private val outputPictureUri: Uri
    ) : ActivityResultContract<Unit?, Uri?>() {

        override fun createIntent(context: Context, input: Unit?): Intent {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputPictureUri)
            grantUriPermission(context, intent, outputPictureUri)
            return Intent.createChooser(intent, "选择要导入的文件")
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return if (resultCode == Activity.RESULT_OK) {
                outputPictureUri
            } else {
                null
            }
        }
    }
}
