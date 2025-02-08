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
import androidx.core.content.pm.PackageInfoCompat

fun Context.checkIsDestroyed(): Boolean {
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

val PackageInfo.versionCodeLong: Long
    get() = PackageInfoCompat.getLongVersionCode(this)

fun Context.getMetaDataString(name: String): String? {
    return this.packageManager.getPackageInfo(
        this.packageName, PackageManager.GET_META_DATA
    ).applicationInfo?.metaData?.getString(name)
}
