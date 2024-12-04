package io.github.chenfei0928.repository.storage.picture

import android.Manifest
import android.net.Uri
import io.github.chenfei0928.repository.storage.BaseFileImportFragment
import io.github.chenfei0928.repository.storage.BasePermissionFileImportParentFragment

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

    override fun createFragment(): BaseFileImportFragment<Uri> {
        return TakePhotoCropImportV19Fragment().also {
            it.arguments = arguments
        }
    }
}