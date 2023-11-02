package io.github.chenfei0928.app.result

import android.app.Activity
import androidx.activity.result.ActivityResultCallback
import permissions.dispatcher.PermissionUtils

/**
 * 权限申请结果回调
 *
 * @param permissions   要申请的权限列表
 */
abstract class PermissionResultCallback(
    private val permissions: Array<String>,
) : ActivityResultCallback<Map<String, Boolean>> {

    override fun onActivityResult(result: Map<String, Boolean>) {
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

    protected abstract fun context(): Activity

    /**
     * 权限已授权的回调
     */
    protected abstract fun onAgree()

    /**
     * 权限被拒绝的回调
     */
    internal abstract fun onDenied()

    /**
     * 权限被拒绝且不再提示的回调
     */
    protected abstract fun onNeverAskAgain()
}
