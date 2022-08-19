package io.github.chenfei0928.app.result

import android.app.Activity
import androidx.activity.result.ActivityResultCallback
import permissions.dispatcher.PermissionUtils

/**
 * 权限申请结果回调
 *
 * @param context       应用会话上下文，用于检查权限授予情况
 * @param permissions   要申请的权限列表
 * @param onAgree       权限已授权的回调
 * @param onDenied      权限被拒绝的回调
 * @param onNeverAskAgain   权限被拒绝且不再提示的回调
 */
internal class PermissionResultCallback(
    private val context: () -> Activity,
    private val permissions: Array<String>,
    private val onAgree: () -> Unit,
    private val onDenied: () -> Unit,
    private val onNeverAskAgain: () -> Unit
) : ActivityResultCallback<Map<String, Boolean>> {

    override fun onActivityResult(result: Map<String, Boolean>?) {
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
