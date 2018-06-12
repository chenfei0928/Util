package com.chenfei.library.util.kotlin

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import com.chenfei.library.R
import permissions.dispatcher.PermissionRequest

fun Activity.onShowPermissionRationale(@StringRes msg: Int, request: PermissionRequest) {
    AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage(msg)
            .setPositiveButton(R.string.permission_allow) { _, _ -> request.proceed() }
            .setNegativeButton(R.string.permission_deny) { _, _ -> request.cancel() }
            .show()
}

fun Activity.onPermissionDenied(@StringRes msg: Int,
                                onPositiveAction: DialogInterface.OnClickListener,
                                onNegativeAction: DialogInterface.OnClickListener?) {
    AlertDialog.Builder(this)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton(R.string.permission_allow, onPositiveAction)
            .setNegativeButton(R.string.permission_deny, onNegativeAction)
            .show()
}

fun Activity.onPermissionNeverAskAgain(@StringRes msg: Int,
                                       onNegativeAction: DialogInterface.OnClickListener?) {
    onPermissionDenied(msg,
            DialogInterface.OnClickListener { _, _ ->
                startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
            },
            onNegativeAction)
}
