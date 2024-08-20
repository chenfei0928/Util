package io.github.chenfei0928.content

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.getSystemService

@TargetApi(Build.VERSION_CODES.O)
fun Context.openChannelSetting(channelId: String) {
    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
    if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
        startActivity(intent)
    }
}

@TargetApi(Build.VERSION_CODES.O)
fun Context.openNotificationSetting() {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
        startActivity(intent)
    }
}

fun Context.createSystemSettingsIntent(action: String): Intent {
    val manager = getSystemService<PackageManager>()
    val packageUri = Uri.parse("package:$packageName")
    val intent = Intent(action, packageUri)
    // 兼容部分手机可能会没有系统Action处理的问题
    return if (!manager?.queryIntentActivities(intent, 0).isNullOrEmpty()) {
        intent
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
    }
}
