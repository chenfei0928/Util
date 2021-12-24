package io.github.chenfei0928.util.kotlin

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import io.github.chenfei0928.library.R
import io.github.chenfei0928.util.PermissionRequest

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
        .setPositiveButton(R.string.permission_allow) { _, _ -> request.proceed() }
        .setNegativeButton(R.string.permission_deny) { _, _ -> request.cancel() }
        .show()
}

/**
 * 当某项权限被拒绝（当某项权限被用户否决，但未不再提示）
 */
fun Activity.onPermissionDenied(
    @StringRes msg: Int,
    onPositiveAction: DialogInterface.OnClickListener,
    onNegativeAction: DialogInterface.OnClickListener?
) {
    onPermissionDenied(getString(msg), onPositiveAction, onNegativeAction)
}

/**
 * 当某项权限被拒绝（当某项权限被用户否决，但未不再提示）
 */
fun Activity.onPermissionDenied(
    msg: CharSequence,
    onPositiveAction: DialogInterface.OnClickListener,
    onNegativeAction: DialogInterface.OnClickListener?
) {
    AlertDialog
        .Builder(this)
        .setTitle(android.R.string.dialog_alert_title)
        .setMessage(msg)
        .setCancelable(false)
        .setPositiveButton(R.string.permission_allow, onPositiveAction)
        .setNegativeButton(R.string.permission_deny, onNegativeAction)
        .show()
}

/**
 * 当某项权限被拒绝（当某项权限被用户否决，但未不再提示）
 */
inline fun Activity.onPermissionDeniedKt(
    @StringRes msg: Int,
    crossinline onPositiveAction: (DialogInterface, Int) -> Unit,
    crossinline onNegativeAction: (DialogInterface, Int) -> Unit = { _, _ -> }
) {
    onPermissionDeniedKt(getString(msg), onPositiveAction, onNegativeAction)
}

/**
 * 当某项权限被拒绝（当某项权限被用户否决，但未不再提示）
 */
inline fun Activity.onPermissionDeniedKt(
    msg: CharSequence,
    crossinline onPositiveAction: (DialogInterface, Int) -> Unit,
    crossinline onNegativeAction: (DialogInterface, Int) -> Unit = { _, _ -> }
) {
    onPermissionDenied(msg, DialogInterface.OnClickListener { dialog, which ->
        onPositiveAction(dialog, which)
    }, DialogInterface.OnClickListener { dialog, which ->
        onNegativeAction(dialog, which)
    })
}

/**
 * 当权限被拒绝并不再提醒，提示用户允许权限
 */
fun Activity.onPermissionNeverAskAgain(
    @StringRes msg: Int, onNegativeAction: DialogInterface.OnClickListener?
) {
    onPermissionDenied(msg, DialogInterface.OnClickListener { _, _ ->
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")
            )
        )
    }, onNegativeAction)
}

/**
 * 当权限被拒绝并不再提醒，提示用户允许权限
 */
fun Activity.onPermissionNeverAskAgain(
    msg: CharSequence, onNegativeAction: DialogInterface.OnClickListener?
) {
    onPermissionDenied(msg, DialogInterface.OnClickListener { _, _ ->
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")
            )
        )
    }, onNegativeAction)
}

/**
 * 当权限被拒绝并不再提醒，提示用户允许权限
 */
inline fun Activity.onPermissionNeverAskAgainKt(
    @StringRes msg: Int, crossinline onNegativeAction: (DialogInterface, Int) -> Unit = { _, _ -> }
) {
    onPermissionNeverAskAgain(msg, DialogInterface.OnClickListener { dialog, which ->
        onNegativeAction(dialog, which)
    })
}

/**
 * 当权限被拒绝并不再提醒，提示用户允许权限
 */
inline fun Activity.onPermissionNeverAskAgainKt(
    msg: CharSequence, crossinline onNegativeAction: (DialogInterface, Int) -> Unit = { _, _ -> }
) {
    onPermissionNeverAskAgain(msg, DialogInterface.OnClickListener { dialog, which ->
        onNegativeAction(dialog, which)
    })
}
