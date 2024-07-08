package io.github.chenfei0928.util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import androidx.ads.identifier.AdvertisingIdClient;
import androidx.ads.identifier.AdvertisingIdInfo;

/**
 * 获取应用安装时安卓系统自动分配的UUID的工具类，支持自动保存。
 * 但是并没有打算去用这个玩意，因为应用重新安装之后会导致UUID被更新（UUID唯一，但是不保证对设备的唯一）
 * 之前使用获取网卡MAC地址的方式在6.0上莫名出现了无法正确获取得问题
 * 为了审核合规，在此处不再获取AndroidId，而使用UUID，此值将会在重装应用后被更新
 *
 * @author Admin
 * @date 2016/2/1
 */
public class Installation {
    private static final String TAG = "KW_Installation";
    private static final String INSTALLATION = "INSTALLATION";
    private static String sID = null;

    private Installation() {
    }

    public static synchronized String id(Context context) {
        if (sID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists()) {
                    writeInstallationFile(context, installation);
                }
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(Context context, File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        out.write(getAdId(context).getBytes());
        out.close();
    }

    private static String getAdId(Context context) {
        // 优先获取广告id
        try {
            if (AdvertisingIdClient.isAdvertisingIdProviderAvailable(context)) {
                long l = System.currentTimeMillis();
                try {
                    AdvertisingIdInfo advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                            .get(300L, TimeUnit.MILLISECONDS);
                    Log.d(TAG, "getAdId: " + (System.currentTimeMillis() - l) + advertisingIdInfo);
                    return advertisingIdInfo.getId();
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    Log.e(TAG, "getAdId: " + (System.currentTimeMillis() - l), e);
                }
            } else {
                Log.d(TAG, "getAdId: AdvertisingId 不可用");
            }
        } catch (Throwable ignore) {
            Log.d(TAG, "getAdId: 不使用 Google AdvertisingId 库");
        }
        // 如果获取到的是模拟器ID，使用生成的随机id
        return UUID.randomUUID().toString();
    }
}
