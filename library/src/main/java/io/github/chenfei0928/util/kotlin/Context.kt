/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-07-12 14:16
 */
package io.github.chenfei0928.util.kotlin

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat

fun Context.checkIsDestroyed(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
        return false
    }
    return when (this) {
        is Activity -> isDestroyed
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
    get() = this.packageManager.getPackageInfo(this.packageName, 0)

val Context.versionName: String
    get() = packageInfo.versionName

val Context.versionCodeLong: Long
    get() = PackageInfoCompat.getLongVersionCode(packageInfo)

fun Context.getMetaDataString(name: String): String? {
    return this.packageManager.getPackageInfo(
        this.packageName, PackageManager.GET_META_DATA
    ).applicationInfo.metaData.getString(name)
}
