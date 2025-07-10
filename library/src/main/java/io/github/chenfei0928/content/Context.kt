/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-07-12 14:16
 */
package io.github.chenfei0928.content

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.lifecycle.isAlive

fun Context.checkIsDestroyed(): Boolean {
    return when (this) {
        is Activity -> isDestroyed
        is LifecycleOwner -> !lifecycle.isAlive
        is Application -> false
        is ContextWrapper -> baseContext.checkIsDestroyed()
        else -> false
    }
}

fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

val Context.packageInfo: PackageInfo
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.packageManager.getPackageInfo(this.packageName, PackageManager.PackageInfoFlags.of(0L))
    } else {
        this.packageManager.getPackageInfo(this.packageName, 0)
    }

val PackageInfo.versionCodeLong: Long
    get() = PackageInfoCompat.getLongVersionCode(this)
