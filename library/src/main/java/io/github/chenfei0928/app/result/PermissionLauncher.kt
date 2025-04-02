package io.github.chenfei0928.app.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import permissions.dispatcher.PermissionUtils

/**
 * 权限申请结果回调
 *
 * @param permissions   要申请的权限列表
 * @param launcher      启用申请权限的实际处理者
 * @param callback      向用户申请权限结果的回调处理
 */
abstract class PermissionLauncher(
    private val permissions: Array<String>,
    private val showRequestPermissionRationaleWhenFirst: Boolean,
    private val launcher: ActivityResultLauncher<Array<String>>,
    private val callback: PermissionResultCallback,
) : ActivityResultLauncher<Unit?>() {

    final override val contract: ActivityResultContract<Unit?, Map<String, Boolean>>
        get() = object : ActivityResultContract<Unit?, Map<String, Boolean>>() {
            val requestMultiplePermissions =
                ActivityResultContracts.RequestMultiplePermissions()

            override fun createIntent(context: Context, input: Unit?): Intent {
                return requestMultiplePermissions.createIntent(context, permissions)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Map<String, Boolean> {
                return requestMultiplePermissions.parseResult(resultCode, intent)
            }
        }

    final override fun launch(input: Unit?, options: ActivityOptionsCompat?) {
        when {
            PermissionUtils.hasSelfPermissions(context(), permissions = permissions) -> {
                callback.onActivityResult(emptyMap())
            }
            PermissionUtils.shouldShowRequestPermissionRationale(
                context(), permissions = permissions
            ) && showRequestPermissionRationaleWhenFirst -> {
                onRationale(object : PermissionRequest {
                    override fun proceed() = launcher.launch(permissions, options)
                    override fun cancel() = callback.onDeniedInternal()
                })
            }
            else -> {
                launcher.launch(permissions, options)
            }
        }
    }

    final override fun unregister() {
        launcher.unregister()
    }

    protected abstract fun context(): Activity

    /**
     * 当权限未被允许需要申请权限时，给予用户对要申请权限的原因提示，
     * 并根据用户选择调用该回调的方法以通知用户是否想要继续授予权限以继续
     */
    protected abstract fun onRationale(request: PermissionRequest)
}
