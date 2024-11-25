package io.github.chenfei0928.os

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.chenfei0928.base.ContextProvider

/**
 * 获取设备是否处于省电模式的状态
 * Created by MrFeng on 2017/11/2.
 */
object PowerSaveUtil {
    private val powerManager: PowerManager? =
        ContextProvider.context.getSystemService<PowerManager>()
    private val sReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            isInPowerSaveMode.value = powerManager?.isPowerSaveMode
        }
    }

    val isInPowerSaveMode: LiveData<Boolean>
        private field: MutableLiveData<Boolean> = MutableLiveData<Boolean>(
            powerManager?.isPowerSaveMode
        )

    init {
        val context = ContextProvider.context
        ContextCompat.registerReceiver(
            context.applicationContext,
            sReceiver,
            IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )
    }
}
