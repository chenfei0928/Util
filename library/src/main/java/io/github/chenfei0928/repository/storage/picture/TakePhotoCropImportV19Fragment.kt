package io.github.chenfei0928.repository.storage.picture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import io.github.chenfei0928.os.getParcelableCompat

/**
 * 提供拍照、并裁剪的导入
 * 由于文件来源是拍照，则需要在结束时撤销文件输出路径的访问权限
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-19 17:27
 */
internal class TakePhotoCropImportV19Fragment : AbsCropImportV19Fragment() {
    private lateinit var takePhotoUri: Uri

    private lateinit var pictureSourceLauncher: ActivityResultLauncher<Unit?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePhotoUri = savedInstanceState?.getParcelableCompat(KEY_TAKE_PHOTO_URI)
            ?: obtainPicturePathUri(requireContext())
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
