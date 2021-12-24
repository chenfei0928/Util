package io.github.chenfei0928.util;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.os.Process;

import java.util.List;

public class RunningEnvironmentUtil {
    private static final String TAG = "KW_RunningEnvironmentU";

    /**
     * 因为推送服务XMPushService在AndroidManifest.xml中设置为运行在另外一个进程
     * 这导致本Application会被实例化两次，所以我们需要让应用的主进程初始化。
     *
     * @param context 程序会话上下文
     * @return 如果是主进程，则返回true
     */
    public static boolean runOnMainProcess(Context context) {
        String mainProcessName = context.getPackageName();
        return mainProcessName.equals(getProcessName(context));
    }

    private static volatile String sProcessName;

    /**
     * 因为推送服务XMPushService在AndroidManifest.xml中设置为运行在另外一个进程
     * 这导致本Application会被实例化两次，所以我们需要让应用的主进程初始化。
     *
     * @param context 程序会话上下文
     * @return 如果是主进程，则返回true
     */
    public static String getProcessName(Context context) {
        if (sProcessName == null) {
            synchronized (RunningEnvironmentUtil.class) {
                if (sProcessName == null) {
                    long l = System.currentTimeMillis();
                    sProcessName = getProcessNameInner(context);
                    Log.i(TAG, "getProcessName: " + (System.currentTimeMillis() - l));
                }
            }
        }
        return sProcessName;
    }

    private static String getProcessNameInner(Context context) {
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid) {
                return info.processName;
            }
        }
        return null;
    }

    public static boolean isServiceWork(Context context, Class<? extends Service> service) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        String serviceName = service.getName();
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
