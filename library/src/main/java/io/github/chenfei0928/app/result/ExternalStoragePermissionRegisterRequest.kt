/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-03 11:01
 */
package io.github.chenfei0928.app.result

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import io.github.chenfei0928.app.activity.onPermissionDenied
import io.github.chenfei0928.app.activity.onPermissionNeverAskAgain
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
    @SuppressLint("MissingPermission") callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> = registerForPermission(
    context = context,
    permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    ),
    permissionName = { getString(R.string.permissionName_sdcard) },
    callback = callback
)

/**
 * 快速请求并处理某项权限
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 */
fun Fragment.registerForPermission(
    permissions: Array<String>,
    permissionName: Context.() -> String,
    @SuppressLint("MissingPermission") callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> =
    registerForPermission(this::requireActivity, permissions, permissionName, callback)

/**
 * 快速请求并处理某项权限
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 */
fun ComponentActivity.registerForPermission(
    permissions: Array<String>,
    permissionName: Context.() -> String,
    @SuppressLint("MissingPermission") callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> =
    registerForPermission({ this }, permissions, permissionName, callback)

/**
 * 快速请求并处理某项权限
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 */
fun ActivityResultCaller.registerForPermission(
    context: () -> Activity,
    permissions: Array<String>,
    permissionName: Context.() -> String,
    @SuppressLint("MissingPermission") callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> {
    var onNeedRetry = {}
    val registerForPermission = registerForPermission(
        context = context,
        permissions = permissions,
        onRationale = {
            context().run {
                onShowPermissionRationale(
                    getString(R.string.permissionRationale, permissionName()), it
                )
            }
        },
        onAgree = {
            callback(true)
        },
        onDenied = {
            context().run {
                onPermissionDenied(
                    getString(R.string.permissionDenied, permissionName()),
                    { _, _ -> onNeedRetry() },
                    { _, _ -> callback(false) })
            }
        },
        onNeverAskAgain = {
            context().run {
                onPermissionNeverAskAgain(
                    getString(R.string.permissionNeverAskAgain, permissionName())
                ) { _, _ ->
                    callback(false)
                }
            }
        })
    onNeedRetry = {
        registerForPermission.launch(null)
    }
    return registerForPermission
}
