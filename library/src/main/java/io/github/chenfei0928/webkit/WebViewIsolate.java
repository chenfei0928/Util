package io.github.chenfei0928.webkit;

import android.content.Context;
import android.os.Build;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import io.github.chenfei0928.util.Log;
import io.github.chenfei0928.app.RunningEnvironmentUtil;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2019-12-12 09:48
 */
public class WebViewIsolate {
    private static final String TAG = "KW_WebViewIsolate";

    /**
     * webView隔离，隔离不同进程webView的数据目录
     * 同一个进程中，只允许在webView初始化前调用，webView创建一次后调用会导致设置失败并activity退出
     *
     * @see <a href="https://developer.android.google.cn/about/versions/pie/android-9.0-changes-28#web-data-dirs">官方介绍</a>
     * @see <a href="https://www.sunzn.com/2019/04/18/Android-P-%E8%A1%8C%E4%B8%BA%E5%8F%98%E6%9B%B4%E5%AF%B9-WebView-%E7%9A%84%E5%BD%B1%E5%93%8D/">相关博文</a>
     */
    public static void checkWebViewDir(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            long l = System.currentTimeMillis();
            String processName = RunningEnvironmentUtil.getProcessName(context);
            // 如果运行的进程不是主进程中，添加前缀
            if (!context.getPackageName().equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
                Log.i(TAG, "checkWebViewDir: " + (System.currentTimeMillis() - l));
            }
        }
    }
}
