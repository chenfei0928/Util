package io.github.chenfei0928.app.result

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import io.github.chenfei0928.app.activity.onPermissionDenied
import io.github.chenfei0928.app.activity.onPermissionNeverAskAgain
import io.github.chenfei0928.app.activity.onShowPermissionRationale
import io.github.chenfei0928.util.R

/**
 * 快速请求并处理某项权限
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 */
fun Fragment.registerForSimplePermission(
    permissions: Array<String>,
    @StringRes permissionName: Int,
    showRationaleWhenFirst: Boolean = true,
    callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> = registerForSimplePermission(
    this::requireActivity,
    permissions,
    showRationaleWhenFirst,
    { getString(permissionName) },
    callback
)

/**
 * 快速请求并处理某项权限
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 */
fun ComponentActivity.registerForSimplePermission(
    permissions: Array<String>,
    @StringRes permissionName: Int,
    showRationaleWhenFirst: Boolean = true,
    callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> = registerForSimplePermission(
    { this }, permissions, showRationaleWhenFirst, { getString(permissionName) }, callback
)

/**
 * 快速请求并处理某项权限
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 */
inline fun Fragment.registerForSimplePermission(
    permissions: Array<String>,
    showRationaleWhenFirst: Boolean = true,
    crossinline permissionName: Context.() -> String,
    noinline callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> = registerForSimplePermission(
    this::requireActivity, permissions, showRationaleWhenFirst, permissionName, callback
)

/**
 * 快速请求并处理某项权限
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 */
inline fun ComponentActivity.registerForSimplePermission(
    permissions: Array<String>,
    showRationaleWhenFirst: Boolean = true,
    crossinline permissionName: Context.() -> String,
    noinline callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> = registerForSimplePermission(
    { this }, permissions, showRationaleWhenFirst, permissionName, callback
)

/**
 * 快速请求并处理某项权限
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 *
 * 该方法[callback]参数不要 inline ，方法实现内部会有多处调用到该字段，回调体过大可能会在编译器inline展开后代码量爆炸
 *
 * @param showRationaleWhenFirst 当权限未被允许，在首次需要申请权限时是否要给予用户对要申请权限的原因提示
 */
inline fun ActivityResultCaller.registerForSimplePermission(
    crossinline context: () -> Activity,
    permissions: Array<String>,
    showRationaleWhenFirst: Boolean = true,
    crossinline permissionName: Context.() -> String,
    noinline callback: (isHasPermission: Boolean) -> Unit
): ActivityResultLauncher<Unit?> {
    var registerForPermission: PermissionLauncher? = null
    val resultCallback = object : PermissionResultCallback(permissions) {
        override val context: Activity
            get() = context()

        override fun onAgree() = callback(true)

        override fun onDenied() = context().run {
            onPermissionDenied(
                getString(R.string.cf0928util_permissionDenied, permissionName()),
                { _, _ -> registerForPermission!!.launch(null) },
                { _, _ -> callback(false) })
        }

        override fun onNeverAskAgain() = context().run {
            onPermissionNeverAskAgain(
                getString(R.string.cf0928util_permissionNeverAskAgain, permissionName())
            ) { _, _ ->
                callback(false)
            }
        }
    }
    val registerForActivityResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(), resultCallback
    )
    registerForPermission = object : PermissionLauncher(
        permissions,
        showRationaleWhenFirst,
        registerForActivityResult,
        resultCallback
    ) {
        override fun context(): Activity = context()
        override fun onRationale(request: PermissionRequest) = context().run {
            onShowPermissionRationale(
                getString(R.string.cf0928util_permissionRationale, permissionName()), request
            )
        }
    }
    return registerForPermission
}
