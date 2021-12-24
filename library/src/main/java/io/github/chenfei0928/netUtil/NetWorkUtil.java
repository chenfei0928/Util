package io.github.chenfei0928.netUtil;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresPermission;

import static android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT;
import static android.telephony.TelephonyManager.NETWORK_TYPE_CDMA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EDGE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EHRPD;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GPRS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GSM;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_IDEN;
import static android.telephony.TelephonyManager.NETWORK_TYPE_IWLAN;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_NR;
import static android.telephony.TelephonyManager.NETWORK_TYPE_TD_SCDMA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;

public class NetWorkUtil {
    private static final int NETWORK_TYPE_LTE_CA = 19;

    public static final int NETWORK_CLASS_UNKNOWN = 0;
    public static final int NETWORK_CLASS_2_G = 1;
    public static final int NETWORK_CLASS_3_G = 2;
    public static final int NETWORK_CLASS_4_G = 3;
    public static final int NETWORK_CLASS_5_G = 4;

    @RequiresPermission(Manifest.permission.ACCESS_WIFI_STATE)
    public static String getNetworkName(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            return wifiInfo.getSSID();
        } else {
            return null;
        }
    }

    /**
     * 判断网络连接状态
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    public static boolean isNetWorkAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
            return netInfo != null && netInfo.isAvailable();
        } else {
            NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork());
            if (capabilities == null) {
                return false;
            }
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND);
        }
    }

    /**
     * 判断是否为Wifi网络连接状态
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    public static boolean isWifiAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
            return netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
        } else {
            NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork());
            if (capabilities == null) {
                return false;
            }
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        }
    }

    /**
     * 判断是否为Mobile网络连接状态
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    public static boolean isMobileAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
            return netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork());
            if (capabilities == null) {
                return false;
            }
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
        }
    }

    /**
     * 获取移动网络类型
     * {@link TelephonyManager.getNetworkClass(int)}
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public static int getMobileNetType(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        switch (telephonyManager.getNetworkType()) {
            case NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_GSM:
            case NETWORK_TYPE_EDGE:
            case NETWORK_TYPE_CDMA:
            case NETWORK_TYPE_1xRTT:
            case NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case NETWORK_TYPE_UMTS:
            case NETWORK_TYPE_EVDO_0:
            case NETWORK_TYPE_EVDO_A:
            case NETWORK_TYPE_HSDPA:
            case NETWORK_TYPE_HSUPA:
            case NETWORK_TYPE_HSPA:
            case NETWORK_TYPE_EVDO_B:
            case NETWORK_TYPE_EHRPD:
            case NETWORK_TYPE_HSPAP:
            case NETWORK_TYPE_TD_SCDMA:
                return NETWORK_CLASS_3_G;
            case NETWORK_TYPE_LTE:
            case NETWORK_TYPE_IWLAN:
            case NETWORK_TYPE_LTE_CA:
                return NETWORK_CLASS_4_G;
            case NETWORK_TYPE_NR:
                return NETWORK_CLASS_5_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }
}
