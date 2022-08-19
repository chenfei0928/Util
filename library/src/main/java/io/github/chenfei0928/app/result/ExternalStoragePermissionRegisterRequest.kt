/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-03 11:01
 */
package io.github.chenfei0928.app.result

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import io.github.chenfei0928.app.activity.onPermissionDeniedKt
import io.github.chenfei0928.app.activity.onPermissionNeverAskAgainKt
import io.github.chenfei0928.app.activity.onShowPermissionRationale
import io.github.chenfei0928.util.R

/**
 * 快速请求并处理扩展卡权限
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 */
fun Fragment.registerForExternalStoragePermission(
    @SuppressLint("MissingPermission") callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> =
    registerForExternalStoragePermission(this::requireActivity, callback)

/**
 * 快速请求并处理扩展卡权限
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 */
fun ComponentActivity.registerForExternalStoragePermission(
    @SuppressLint("MissingPermission") callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> =
    registerForExternalStoragePermission({ this }, callback)

/**
 * 快速请求并处理扩展卡权限
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 */
fun ActivityResultCaller.registerForExternalStoragePermission(
    context: () -> Activity,
    callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> {
    var onNeedRetry = {}
    val registerForPermission = registerForPermission(
        context = context,
        permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        ),
        onRationale = {
            context().onShowPermissionRationale(
                R.string.permissionRationale_sdCard, it
            )
        },
        onAgree = {
            callback(true)
        },
        onDenied = {
            context().onPermissionDeniedKt(R.string.permissionDenied_sdCard,
                { _, _ -> onNeedRetry() },
                { _, _ -> callback(false) })
        },
        onNeverAskAgain = {
            context().onPermissionNeverAskAgainKt(R.string.permissionNeverAskAgain_sdCard) { _, _ ->
                callback(false)
            }
        })
    onNeedRetry = {
        registerForPermission.launch(null)
    }
    return registerForPermission
}
