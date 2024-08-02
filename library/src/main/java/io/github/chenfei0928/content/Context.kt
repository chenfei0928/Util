/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-07-12 14:16
 */
package io.github.chenfei0928.content

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.RemoteException
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

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

suspend inline fun <T> Context.bindService(
    intent: Intent,
    flag: Int,
    crossinline onServiceConnected: OnServiceConnected<T>
): T {
    var outerConnection: ServiceConnection? = null
    return try {
        suspendCancellableCoroutine { continuation ->
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    try {
                        onServiceConnected(name, service, continuation)
                    } catch (e: RemoteException) {
                        // ipc执行失败，返回异常
                        if (!continuation.isActive) {
                            return
                        }
                        continuation.resumeWithException(e)
                    }
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    // service链接断开，返回异常
                    if (!continuation.isActive) {
                        return
                    }
                    continuation.resumeWithException(RemoteException("onServiceDisconnected"))
                }
            }
            outerConnection = connection
            bindService(intent, connection, flag)
            continuation.invokeOnCancellation {
                // 当请求被取消，直接断开服务
                unbindService(connection)
            }
        }
    } finally {
        // 清理现场，关闭ipc链接
        outerConnection?.let {
            unbindService(it)
        }
    }
}

typealias OnServiceConnected<T> = (
    name: ComponentName,
    service: IBinder,
    continuation: CancellableContinuation<T>
) -> Unit
