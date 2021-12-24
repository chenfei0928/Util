/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-03 11:01
 */
package io.github.chenfei0928.util.kotlin

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import io.github.chenfei0928.util.R

/**
 * 快速请求并处理扩展卡权限
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 */
fun Fragment.registerForExternalStoragePermission(
    @SuppressLint("MissingPermission") callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> {
    var onNeedRetry = {}
    val registerForPermission = registerForPermission(permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    ), onRationale = {
        requireActivity().onShowPermissionRationale(
            R.string.permissionRationale_sdCard, it
        )
    }, onAgree = {
        callback(true)
    }, onDenied = {
        requireActivity().onPermissionDeniedKt(R.string.permissionDenied_sdCard,
            { _, _ -> onNeedRetry() },
            { _, _ -> callback(false) })
    }, onNeverAskAgain = {
        requireActivity().onPermissionNeverAskAgainKt(R.string.permissionNeverAskAgain_sdCard) { _, _ ->
            callback(false)
        }
    })
    onNeedRetry = {
        registerForPermission.launch(null)
    }
    return registerForPermission
}

/**
 * 快速请求并处理扩展卡权限
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 */
fun ComponentActivity.registerForExternalStoragePermission(
    callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> {
    var onNeedRetry = {}
    val registerForPermission = registerForPermission(permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    ), onRationale = {
        onShowPermissionRationale(
            R.string.permissionRationale_sdCard, it
        )
    }, onAgree = {
        callback(true)
    }, onDenied = {
        onPermissionDeniedKt(R.string.permissionDenied_sdCard,
            { _, _ -> onNeedRetry() },
            { _, _ -> callback(false) })
    }, onNeverAskAgain = {
        onPermissionNeverAskAgainKt(R.string.permissionNeverAskAgain_sdCard) { _, _ ->
            callback(false)
        }
    })
    onNeedRetry = {
        registerForPermission.launch(null)
    }
    return registerForPermission
}
