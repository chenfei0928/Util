/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-02 14:35
 */
package io.github.chenfei0928.app.result

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import permissions.dispatcher.PermissionUtils

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
): ActivityResultLauncher<Unit?> {
    val resultCallback = PermissionResultCallback<Map<String, Boolean>>(
        this::requireActivity, permissions, onAgree, onDenied, onNeverAskAgain
    )
    val registerForActivityResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(), resultCallback
    )
    return PermissionLauncher(
        this::requireActivity,
        permissions,
        registerForActivityResult,
        resultCallback,
        onRationale,
        onDenied
    )
}

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
    onAgree: () -> Unit,
    onDenied: () -> Unit,
    onNeverAskAgain: () -> Unit
): ActivityResultLauncher<Unit?> {
    val resultCallback = PermissionResultCallback<Map<String, Boolean>>(
        { this }, permissions, onAgree, onDenied, onNeverAskAgain
    )
    val registerForActivityResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(), resultCallback
    )
    return PermissionLauncher(
        { this }, permissions, registerForActivityResult, resultCallback, onRationale, onDenied
    )
}

/**
 * 权限申请结果回调
 *
 * @param context       应用会话上下文，用于检查权限授予情况
 * @param permissions   要申请的权限列表
 * @param launcher      启用申请权限的实际处理者
 * @param callback      向用户申请权限结果的回调处理
 * @param onRationale   当权限未被允许需要申请权限时，给予用户对要申请权限的原因提示，
 *                      并根据用户选择调用该回调的方法以通知用户是否想要继续授予权限以继续
 * @param onDenied      权限被拒绝的回调
 */
private class PermissionLauncher(
    private val context: () -> Activity,
    private val permissions: Array<String>,
    private val launcher: ActivityResultLauncher<Array<String>>,
    private val callback: ActivityResultCallback<Map<String, Boolean>>,
    private val onRationale: (request: PermissionRequest) -> Unit,
    private val onDenied: () -> Unit
) : ActivityResultLauncher<Unit?>() {

    override fun getContract(): ActivityResultContract<Unit?, Map<String, Boolean>> {
        val requestMultiplePermissions = ActivityResultContracts.RequestMultiplePermissions()
        return object : ActivityResultContract<Unit?, Map<String, Boolean>>() {
            override fun createIntent(context: Context, void: Unit?): Intent {
                return requestMultiplePermissions.createIntent(context, permissions)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Map<String, Boolean> {
                return requestMultiplePermissions.parseResult(resultCode, intent)
            }
        }
    }

    override fun launch(input: Unit?, options: ActivityOptionsCompat?) {
        when {
            PermissionUtils.hasSelfPermissions(context(), *permissions) -> {
                callback.onActivityResult(null)
            }
            PermissionUtils.shouldShowRequestPermissionRationale(context(), *permissions) -> {
                onRationale(object :
                    PermissionRequest {

                    override fun proceed() {
                        launcher.launch(permissions, options)
                    }

                    override fun cancel() {
                        onDenied()
                    }
                })
            }
            else -> {
                launcher.launch(permissions, options)
            }
        }
    }

    override fun unregister() {
        launcher.unregister()
    }
}

/**
 * 权限申请结果回调
 *
 * @param context       应用会话上下文，用于检查权限授予情况
 * @param permissions   要申请的权限列表
 * @param onAgree       权限已授权的回调
 * @param onDenied      权限被拒绝的回调
 * @param onNeverAskAgain   权限被拒绝且不再提示的回调
 */
private class PermissionResultCallback<T>(
    private val context: () -> Activity,
    private val permissions: Array<String>,
    private val onAgree: () -> Unit,
    private val onDenied: () -> Unit,
    private val onNeverAskAgain: () -> Unit
) : ActivityResultCallback<T> {

    override fun onActivityResult(result: T?) {
        when {
            PermissionUtils.hasSelfPermissions(context(), *permissions) -> {
                // 如果有所有的权限，处理
                onAgree()
            }
            PermissionUtils.shouldShowRequestPermissionRationale(context(), *permissions) -> {
                // 当被拒绝，可以再次提示请求权限的原因
                onDenied()
            }
            else -> {
                // 如果不需要显示请求权限的原因被拒绝
                onNeverAskAgain()
            }
        }
    }
}
