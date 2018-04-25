package com.chenfei.library.util;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;

/**
 * 获取设备是否处于省电模式的状态
 * Created by MrFeng on 2017/11/2.
 */
public class PowerSaveUtil {
    private static volatile Receiver sReceiver = null;
    private static boolean sIsInPowerSaveMode = false;

    public static boolean isIsInPowerSaveMode(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        if (sReceiver == null) {
            synchronized (PowerSaveUtil.class) {
                if (sReceiver == null) {
                    PowerManager service = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    sIsInPowerSaveMode = service != null && service.isPowerSaveMode();
                    sReceiver = new Receiver();
                    context.getApplicationContext().registerReceiver(sReceiver,
                            new IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED));
                }
            }
        }
        return sIsInPowerSaveMode;
    }

    public static void trimMemory(Application application, int level) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN && sReceiver != null) {
            application.unregisterReceiver(sReceiver);
            sReceiver = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            PowerManager service = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            sIsInPowerSaveMode = service != null && service.isPowerSaveMode();
        }
    }
}
