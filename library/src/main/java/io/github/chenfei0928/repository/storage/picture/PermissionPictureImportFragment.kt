package io.github.chenfei0928.repository.storage.picture

import android.Manifest
import android.net.Uri
import android.os.Build
import io.github.chenfei0928.repository.storage.BaseFileImportFragment
import io.github.chenfei0928.repository.storage.BasePermissionFileImportParentFragment

/**
 * 请求权限的裁图相册文件导入
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-23 16:03
 */
internal class PictureImportPermissionFragment : BasePermissionFileImportParentFragment<Uri>(
    requestPermission = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    )
) {
    // 5.0以上使用uri返回
    override val permissionMaxSdkVersion: Int
        get() = Build.VERSION_CODES.LOLLIPOP
    override val permissionName: String
        get() = "存储卡"

    override fun createFragment(): BaseFileImportFragment<Uri>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PictureCropImportV19Fragment()
        } else {
            PictureCropImportBaseFragment()
        }.also {
            it.arguments = arguments
        }
    }
}

/**
 * 请求权限的相机裁图文件导入
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-23 16:03
 */
internal class TakePhotoImportPermissionFragment : BasePermissionFileImportParentFragment<Uri>(
    requestPermission = arrayOf(Manifest.permission.CAMERA)
) {
    override val permissionName: String
        get() = "相机"

    override fun createFragment(): BaseFileImportFragment<Uri>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TakePhotoCropImportV19Fragment()
        } else {
            TakePhotoCropImportBaseFragment()
        }.also {
            it.arguments = arguments
        }
    }
}
