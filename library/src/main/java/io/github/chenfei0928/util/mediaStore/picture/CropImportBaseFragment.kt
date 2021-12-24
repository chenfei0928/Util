package io.github.chenfei0928.util.mediaStore.picture

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore

/**
 * 提供图片选择、并裁剪的导入
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-19 17:27
 */
internal class PictureCropImportBaseFragment : AbsCropImportBaseFragment() {

    override val contract: AbsCropImportContract = object : AbsCropImportContract() {
        override fun createPictureSourceIntent(headImageSelectUri: Uri): Intent {
            // 4.4 之下，直接选择并裁切图片即可
            val intent = Intent(Intent.ACTION_GET_CONTENT, null)
            intent.type = "image/*"
            intent.putExtra("return-data", false)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, headImageSelectUri)
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            // 没有人脸检测
            intent.putExtra("noFaceDetection", true)
            return intent
        }
    }
}

/**
 * 提供照片拍照、并裁剪的导入
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-19 17:27
 */
internal class TakePhotoCropImportBaseFragment : AbsCropImportBaseFragment() {

    override val contract: AbsCropImportContract = object : AbsCropImportContract() {
        override fun createPictureSourceIntent(headImageSelectUri: Uri): Intent {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, headImageSelectUri)
            return intent
        }
    }
}
