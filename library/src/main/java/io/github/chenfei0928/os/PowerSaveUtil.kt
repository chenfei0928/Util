package io.github.chenfei0928.os

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import io.github.chenfei0928.util.WeakCache

/**
 * 获取设备是否处于省电模式的状态
 * Created by MrFeng on 2017/11/2.
 */
object PowerSaveUtil {
    private val powerManagerCache = WeakCache<Context, PowerManager?> {
        it.getSystemService<PowerManager>()
    }

    private val sReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val service = powerManagerCache[context.applicationContext]
            sIsInPowerSaveMode = service != null && service.isPowerSaveMode
        }
    }
    private var sIsInPowerSaveMode = false

    fun isInPowerSaveMode(context: Context): Boolean {
        val service = powerManagerCache[context.applicationContext]
        sIsInPowerSaveMode = service != null && service.isPowerSaveMode
        ContextCompat.registerReceiver(
            context.applicationContext,
            sReceiver,
            IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )
        return sIsInPowerSaveMode
    }
}
