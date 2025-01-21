package io.github.chenfei0928.app

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import androidx.core.content.getSystemService

object RunningEnvironmentUtil {

    fun isServiceWork(context: Context, service: Class<out Service>): Boolean {
        @Suppress("DEPRECATION")
        val runningService = context.getSystemService<ActivityManager>()
            ?.getRunningServices(40)
            ?: return false
        val serviceName = service.name
        return runningService.any { it.service.className == serviceName }
    }
}
