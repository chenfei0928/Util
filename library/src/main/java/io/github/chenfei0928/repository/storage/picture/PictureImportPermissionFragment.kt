package io.github.chenfei0928.repository.storage.picture

import android.Manifest
import android.net.Uri
import android.os.Build
import io.github.chenfei0928.repository.storage.BaseFileImportFragment
import io.github.chenfei0928.repository.storage.BasePermissionFileImportParentFragment
import io.github.chenfei0928.util.R

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
        get() = getString(R.string.cf0928util_permissionName_sdcard)

    override fun createFragment(): BaseFileImportFragment<Uri> {
        return PictureCropImportV19Fragment().also {
            it.arguments = arguments
        }
    }
}
