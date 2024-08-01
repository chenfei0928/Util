package io.github.chenfei0928.app

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.app.Service
import android.content.Context
import android.os.Build
import android.os.Process
import androidx.core.content.getSystemService
import io.github.chenfei0928.concurrent.updateAndGetCompat
import java.util.concurrent.atomic.AtomicReference

object RunningEnvironmentUtil {
    private val processName = AtomicReference<String>()

    /**
     * 因为推送服务XMPushService在AndroidManifest.xml中设置为运行在另外一个进程
     * 这导致本Application会被实例化两次，所以我们需要让应用的主进程初始化。
     *
     * @param context 程序会话上下文
     * @return 如果是主进程，则返回true
     */
    fun runOnMainProcess(context: Context): Boolean {
        return context.packageName == getProcessName(context)
    }

    /**
     * 因为推送服务XMPushService在AndroidManifest.xml中设置为运行在另外一个进程
     * 这导致本Application会被实例化两次，所以我们需要让应用的主进程初始化。
     *
     * @param context 程序会话上下文
     * @return 如果是主进程，则返回true
     */
    @JvmStatic
    fun getProcessName(context: Context): String {
        return processName.get() ?: processName.updateAndGetCompat {
            @SuppressLint("ObsoleteSdkInt")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Application.getProcessName()
            } else try {
                @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
                val method = Class.forName("android.app.ActivityThread")
                    .getDeclaredMethod("currentProcessName")
                method.isAccessible = true
                method.invoke(null) as String
            } catch (ignore: ReflectiveOperationException) {
                val myPid = Process.myPid()
                val am = context.getSystemService<ActivityManager>()!!
                am.runningAppProcesses.find { it.pid == myPid }!!.processName
            }
        }
    }

    fun isServiceWork(context: Context, service: Class<out Service>): Boolean {
        val runningService = context.getSystemService<ActivityManager>()
            ?.getRunningServices(40)
            ?: return false
        val serviceName = service.name
        return runningService.any { it.service.className == serviceName }
    }
}
