package io.github.chenfei0928.app.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import permissions.dispatcher.PermissionUtils

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
internal class PermissionLauncher(
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
            override fun createIntent(context: Context, input: Unit?): Intent {
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
                onRationale(object : PermissionRequest {

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
