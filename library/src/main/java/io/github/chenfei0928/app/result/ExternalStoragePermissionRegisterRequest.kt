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
inline fun ActivityResultCaller.registerForExternalStoragePermission(
    crossinline context: () -> Activity,
    @SuppressLint("MissingPermission") noinline callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> = registerForPermission(
    context = context,
    permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    ),
    permissionName = { getString(R.string.permissionName_sdcard) },
    callback = callback
)
