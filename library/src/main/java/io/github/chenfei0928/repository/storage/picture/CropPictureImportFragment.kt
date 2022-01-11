package io.github.chenfei0928.repository.storage.picture

import android.os.Bundle
import io.github.chenfei0928.repository.storage.BaseFileImportUriFragment

/**
 * 裁图导入器，支持从相机、相册来源
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-19 19:11
 */
class CropPictureImportFragment : BaseFileImportUriFragment() {

    override fun launchFileChoose() {
        val source = arguments?.getInt(KEY_SOURCE)
            ?: run {
                removeSelf(null)
                return
            }
        // 根据参数创建指定的图片裁剪导入器
        val fragment = when (source) {
            PICTURE -> PictureImportPermissionFragment()
            TAKE_PHOTO -> TakePhotoImportPermissionFragment()
            else -> {
                removeSelf(null)
                return
            }
        }
        // 为图片裁剪导入器设置参数和回调
        fragment.arguments = arguments
        fragment.resultCallback = {
            removeSelf(it)
        }
        // 启动导入器
        childFragmentManager.beginTransaction()
            .add(fragment, "fileExport")
            .commitAllowingStateLoss()
    }

    companion object {
        private const val KEY_SOURCE = "source"
        private const val CROP_INTENT_PARAM = AbsCropImportBaseFragment.CROP_INTENT_PARAM
        const val PICTURE = 0
        const val TAKE_PHOTO = 1

        @JvmStatic
        @JvmOverloads
        fun newInstance(source: Int, cropParam: Bundle? = Square) =
            CropPictureImportFragment().apply {
                arguments = createArg(source, cropParam)
            }

        private fun createArg(source: Int, cropParam: Bundle?): Bundle {
            return Bundle().apply {
                putInt(KEY_SOURCE, source)
                putBundle(CROP_INTENT_PARAM, cropParam)
            }
        }

        private val Square = Bundle().apply {
            putString("crop", "true")
            putInt("aspectX", 1)
            putInt("aspectY", 1)
            putInt("outputX", 640)
            putInt("outputY", 640)
            putBoolean("scale", true)
            // 去黑边
            putBoolean("scaleUpIfNeeded", true)
        }
    }
}
