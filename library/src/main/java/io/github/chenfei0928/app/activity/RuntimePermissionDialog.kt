package io.github.chenfei0928.app.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import io.github.chenfei0928.app.result.PermissionRequest
import io.github.chenfei0928.util.R

/**
 * 进行有理由的申请某项权限（区别于部分不需要理由即可申请的权限）
 */
fun Activity.onShowPermissionRationale(@StringRes msg: Int, request: PermissionRequest) {
    onShowPermissionRationale(getString(msg), request)
}

/**
 * 进行有理由的申请某项权限（区别于部分不需要理由即可申请的权限）
 */
fun Activity.onShowPermissionRationale(msg: CharSequence, request: PermissionRequest) {
    AlertDialog
        .Builder(this)
        .setCancelable(false)
        .setMessage(msg)
        .setPositiveButton(R.string.cf0928util_permission_allow) { _, _ -> request.proceed() }
        .setNegativeButton(R.string.cf0928util_permission_deny) { _, _ -> request.cancel() }
        .show()
}

/**
 * 当某项权限被拒绝（当某项权限被用户否决，但未不再提示）
 */
fun Activity.onPermissionDenied(
    @StringRes msg: Int,
    onPositiveAction: DialogInterface.OnClickListener,
    onNegativeAction: DialogInterface.OnClickListener? = null
) = onPermissionDenied(getString(msg), onPositiveAction, onNegativeAction)

/**
 * 当某项权限被拒绝（当某项权限被用户否决，但未不再提示）
 */
fun Activity.onPermissionDenied(
    msg: CharSequence,
    onPositiveAction: DialogInterface.OnClickListener,
    onNegativeAction: DialogInterface.OnClickListener? = null
) {
    AlertDialog
        .Builder(this)
        .setTitle(android.R.string.dialog_alert_title)
        .setMessage(msg)
        .setCancelable(false)
        .setPositiveButton(R.string.cf0928util_permission_allow, onPositiveAction)
        .setNegativeButton(R.string.cf0928util_permission_deny, onNegativeAction)
        .show()
}

/**
 * 当权限被拒绝并不再提醒，提示用户允许权限
 */
fun Activity.onPermissionNeverAskAgain(
    @StringRes msg: Int,
    onNegativeAction: DialogInterface.OnClickListener? = null
) = onPermissionDenied(msg, { _, _ ->
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")
        )
    )
}, onNegativeAction)

/**
 * 当权限被拒绝并不再提醒，提示用户允许权限
 */
fun Activity.onPermissionNeverAskAgain(
    msg: CharSequence,
    onNegativeAction: DialogInterface.OnClickListener? = null
) = onPermissionDenied(msg, { _, _ ->
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")
        )
    )
}, onNegativeAction)
