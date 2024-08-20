package io.github.chenfei0928.net

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService

object NetWorkUtil {
    const val NETWORK_CLASS_UNKNOWN: Int = 0
    const val NETWORK_CLASS_2_G: Int = 1
    const val NETWORK_CLASS_3_G: Int = 2
    const val NETWORK_CLASS_4_G: Int = 3
    const val NETWORK_CLASS_5_G: Int = 4
    private const val NETWORK_TYPE_LTE_CA = 19

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
        ]
    )
    fun getNetworkName(context: Context): String? {
        return context.getSystemService<WifiManager>()?.connectionInfo?.ssid
    }

    /**
     * 判断网络连接状态
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetWorkAvailable(context: Context): Boolean {
        val connMgr = context.getSystemService<ConnectivityManager>()
            ?: return false
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            val netInfo = connMgr.activeNetworkInfo
            netInfo != null && netInfo.isAvailable
        } else {
            val capabilities = connMgr.getNetworkCapabilities(connMgr.activeNetwork)
                ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND)
        }
    }

    /**
     * 判断是否为Wifi网络连接状态
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isWifiAvailable(context: Context): Boolean {
        val connMgr = context.getSystemService<ConnectivityManager>()
            ?: return false
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            val netInfo = connMgr.activeNetworkInfo
            netInfo != null && netInfo.type == ConnectivityManager.TYPE_WIFI
        } else {
            val capabilities = connMgr.getNetworkCapabilities(connMgr.activeNetwork)
                ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
    }

    /**
     * 判断是否为Mobile网络连接状态
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isMobileAvailable(context: Context): Boolean {
        val connMgr = context.getSystemService<ConnectivityManager>()
            ?: return false
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            val netInfo = connMgr.activeNetworkInfo
            netInfo != null && netInfo.type == ConnectivityManager.TYPE_MOBILE
        } else {
            val capabilities = connMgr.getNetworkCapabilities(connMgr.activeNetwork)
                ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        }
    }

    /**
     * 获取移动网络类型
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getMobileNetType(
        context: Context
    ): Int = when (context.getSystemService<TelephonyManager>()?.networkType) {
        TelephonyManager.NETWORK_TYPE_GPRS,
        TelephonyManager.NETWORK_TYPE_GSM,
        TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN -> NETWORK_CLASS_2_G
        TelephonyManager.NETWORK_TYPE_UMTS,
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSPAP,
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> NETWORK_CLASS_3_G
        TelephonyManager.NETWORK_TYPE_LTE,
        TelephonyManager.NETWORK_TYPE_IWLAN,
        NETWORK_TYPE_LTE_CA -> NETWORK_CLASS_4_G
        TelephonyManager.NETWORK_TYPE_NR -> NETWORK_CLASS_5_G
        else -> NETWORK_CLASS_UNKNOWN
    }
}
