package com.chenfei.library.util.kotlin

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

fun Context.openChannelSetting(channelId: String) {
    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
    if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null)
        startActivity(intent)
}

fun Context.openNotificationSetting() {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null)
        startActivity(intent)
}

fun Activity.gotoSystemSettings(action: String, requestCode: Int) {
    try {
        // 兼容部分手机可能会没有系统Action处理的问题
        val intent = Intent(action, Uri.parse("package:$packageName"))
        startActivityForResult(intent, requestCode)
    } catch (e: ActivityNotFoundException) {
        // 则跳转应用详情页
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:$packageName"))
        startActivity(intent)
    }
}
