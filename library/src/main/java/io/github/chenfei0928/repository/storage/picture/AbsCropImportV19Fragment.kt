package io.github.chenfei0928.repository.storage.picture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import io.github.chenfei0928.app.result.registerAllActivityResultLauncher
import io.github.chenfei0928.app.result.registerForActivityResultDelegate
import io.github.chenfei0928.content.FileProviderUtil
import io.github.chenfei0928.os.getParcelableCompat
import io.github.chenfei0928.repository.storage.BaseFileImportUriFragment
import java.io.File

/**
 * 提供图片选择、并裁剪的导入（图片来源由子类实现）
 * 主要提供图片裁剪的实现，如不需要裁剪，直接使用文件导入或不传递cropParam即可
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-19 15:10
 */
internal abstract class AbsCropImportV19Fragment : BaseFileImportUriFragment() {
    private lateinit var croppedImageUri: Uri
    private val cropImageLauncher: ActivityResultLauncher<Pair<Uri, Bundle?>> by registerForActivityResultDelegate {
        registerForActivityResult(CropImageContract(croppedImageUri)) { uri ->
            if (uri != null) {
                // 裁剪图片完成
                // 撤销对Uri权限
                revokeUriPermission(requireContext(), uri)
                removeSelf(uri)
            } else {
                revokeUriPermission(requireContext(), croppedImageUri)
                removeSelf(null)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        croppedImageUri = savedInstanceState?.getParcelableCompat(KEY_CROPPED_IMAGE_URI)
            ?: obtainPicturePathUri(requireContext())
        registerAllActivityResultLauncher()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_CROPPED_IMAGE_URI, croppedImageUri)
    }

    protected fun cropImage(uri: Uri) {
        val cropParam = arguments?.getBundle(CROP_INTENT_PARAM)
        if (cropParam == null) {
            // 不需要进行裁图，直接返回
            removeSelf(uri)
        } else {
            cropImageLauncher.launch(Pair(uri, cropParam))
        }
    }

    /**
     * 4.4 以上，使用FileProvider来存储文件，将其存放在缓存目录
     *
     * @return 缓存目录
     */
    protected fun obtainPicturePathUri(context: Context): Uri {
        return FileProviderUtil.createUriFromFile(context, obtainPictureFile(context))
    }

    private class CropImageContract(
        private val croppedImageUri: Uri
    ) : ActivityResultContract<Pair<Uri, Bundle?>, Uri?>() {

        override fun getSynchronousResult(
            context: Context, input: Pair<Uri, Bundle?>
        ): SynchronousResult<Uri?>? {
            return if (input.second == null) {
                // 不需要进行裁图，直接返回
                SynchronousResult(input.first)
            } else {
                null
            }
        }

        override fun createIntent(
            context: Context, input: Pair<Uri, Bundle?>
        ): Intent {
            // 创建裁图intent
            val intent = createBaseCropImageIntent(input.first, croppedImageUri)
            // 将传入的裁图参数传入
            intent.putExtras(input.second!!)
            // 允许对Uri权限
            grantUriPermission(context, intent, croppedImageUri)
            return intent
        }

        private fun createBaseCropImageIntent(source: Uri, output: Uri): Intent {
            val intent = Intent("com.android.camera.action.CROP")
            // 给权限
            // http://www.voidcn.com/blog/kongbaidepao/article/p-6174797.html
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(source, "image/*")
            intent.putExtra("return-data", false)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, output)
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            intent.putExtra("noFaceDetection", true)
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return if (resultCode == Activity.RESULT_OK) {
                intent?.data ?: croppedImageUri
            } else {
                null
            }
        }
    }

    companion object {
        private const val KEY_CROPPED_IMAGE_URI = "croppedImageUri"
        private const val CROP_INTENT_PARAM = AbsCropImportBaseFragment.CROP_INTENT_PARAM

        /**
         * 授予对某个意向的uri授权
         *
         * @param intent  要授予访问某个uri的intent意向
         * @param fileUri 要授予权限的uri
         */
        @JvmStatic
        protected fun grantUriPermission(
            context: Context, intent: Intent, fileUri: Uri
        ) {
            val resolvedIntentActivities: List<ResolveInfo> =
                context.packageManager.queryIntentActivities(
                    intent, PackageManager.MATCH_DEFAULT_ONLY
                )
            for (resolvedIntentInfo in resolvedIntentActivities) {
                val packageName = resolvedIntentInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    fileUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }

        /**
         * 撤销权限
         *
         * @param fileUri 撤销对某个uri的读写权限
         */
        @JvmStatic
        protected fun revokeUriPermission(context: Context, fileUri: Uri) {
            context.revokeUriPermission(
                fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        @JvmStatic
        protected fun obtainPictureFile(context: Context): File =
            File(context.cacheDir.path, "cache_" + System.currentTimeMillis() + ".png")
    }
}
