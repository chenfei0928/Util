/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-02 14:35
 */
package io.github.chenfei0928.app.result

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

/**
 * 注册权限申请处理回调，当所有权限均已拥有时会直接执行回调
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 *
 * @param permissions   要申请的权限
 * @param onRationale   当权限未被允许需要申请权限时，给予用户对要申请权限的原因提示，
 *                      并根据用户选择调用该回调的方法以通知用户是否想要继续授予权限以继续
 * @param onAgree       当权限被允许/权限已获取到时的回调，可以执行后续操作
 * @param onDenied      当权限被拒绝，给予用户提示，是否重试
 * @param onNeverAskAgain   当权限被拒绝并不再提示，给予用户提示并取消操作
 */
fun Fragment.registerForPermission(
    permissions: Array<String>,
    onRationale: (request: PermissionRequest) -> Unit,
    @SuppressLint("MissingPermission") onAgree: () -> Unit,
    onDenied: () -> Unit,
    onNeverAskAgain: () -> Unit
): ActivityResultLauncher<Unit?> = registerForPermission(
    this::requireActivity, permissions, onRationale, onAgree, onDenied, onNeverAskAgain
)

/**
 * 注册权限申请处理回调，当所有权限均已拥有时会直接执行回调
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 *
 * @param permissions   要申请的权限
 * @param onRationale   当权限未被允许需要申请权限时，给予用户对要申请权限的原因提示，
 *                      并根据用户选择调用该回调的方法以通知用户是否想要继续授予权限以继续
 * @param onAgree       当权限被允许/权限已获取到时的回调，可以执行后续操作
 * @param onDenied      当权限被拒绝，给予用户提示，是否重试
 * @param onNeverAskAgain   当权限被拒绝并不再提示，给予用户提示并取消操作
 */
fun ComponentActivity.registerForPermission(
    permissions: Array<String>,
    onRationale: (request: PermissionRequest) -> Unit,
    @SuppressLint("MissingPermission") onAgree: () -> Unit,
    onDenied: () -> Unit,
    onNeverAskAgain: () -> Unit
): ActivityResultLauncher<Unit?> = registerForPermission(
    { this }, permissions, onRationale, onAgree, onDenied, onNeverAskAgain
)

/**
 * 注册权限申请处理回调，当所有权限均已拥有时会直接执行回调
 * 不要使用[lazy]进行懒加载，其注册权限请求时会检查当前状态
 *
 * @param permissions   要申请的权限
 * @param onRationale   当权限未被允许需要申请权限时，给予用户对要申请权限的原因提示，
 *                      并根据用户选择调用该回调的方法以通知用户是否想要继续授予权限以继续
 * @param onAgree       当权限被允许/权限已获取到时的回调，可以执行后续操作
 * @param onDenied      当权限被拒绝，给予用户提示，是否重试
 * @param onNeverAskAgain   当权限被拒绝并不再提示，给予用户提示并取消操作
 */
fun ActivityResultCaller.registerForPermission(
    context: () -> Activity,
    permissions: Array<String>,
    onRationale: (request: PermissionRequest) -> Unit,
    @SuppressLint("MissingPermission") onAgree: () -> Unit,
    onDenied: () -> Unit,
    onNeverAskAgain: () -> Unit
): ActivityResultLauncher<Unit?> {
    val resultCallback = PermissionResultCallback(
        context, permissions, onAgree, onDenied, onNeverAskAgain
    )
    val registerForActivityResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(), resultCallback
    )
    return PermissionLauncher(
        context, permissions, registerForActivityResult, resultCallback, onRationale, onDenied
    )
}
