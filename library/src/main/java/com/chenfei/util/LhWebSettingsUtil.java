package com.chenfei.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.chenfei.base.app.BaseApplication;
import com.chenfei.lib_base.BuildConfig;
import com.chenfei.lib_base.R;
import com.chenfei.util.kotlin.ViewKt;
import com.chenfei.view.SystemUiUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import androidx.webkit.WebViewRenderProcess;
import androidx.webkit.WebViewRenderProcessClient;
import kotlin.jvm.functions.Function1;

/**
 * @author MrFeng
 * @date 2017/3/8
 * @see <a href="http://reezy.me/p/20170515/android-webview/">原博客</a>
 */
public class LhWebSettingsUtil {
    private static final String TAG = "KW_WebSettingUtil";
    private static boolean safeBrowsingEnable = true;

    static {
        BaseApplication appContext = BaseApplication.getInstance();
        // webView可被调试
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        // 当前的webView提供者
        if (BuildConfig.loggable) {
            PackageInfo packageInfo = WebViewCompat.getCurrentWebViewPackage(appContext);
            Log.d(TAG, "WebView version: " + packageInfo);
        }
        // 安全浏览
        if (WebViewFeature.isFeatureSupported(WebViewFeature.START_SAFE_BROWSING)) {
            WebViewCompat.startSafeBrowsing(appContext, success ->
                    Log.i(TAG, "startSafeBrowsing: " + success));
        }
        // 安全浏览白名单
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_WHITELIST)) {
            List<String> array = new ArrayList<>();
            array.add("yiketalks.com");
            array.add(".yiketalks.com");
            WebViewCompat.setSafeBrowsingWhitelist(array, isReceive ->
                    Log.i(TAG, "setSafeBrowsingWhitelist: " + isReceive));
        }
    }

    @Nullable
    public static <WV extends WebView> WV installWebView(@NonNull View placeHolder, @NonNull Function1<Context, WV> creator) {
        ViewGroup parent = (ViewGroup) placeHolder.getParent();
        // 修复 5.x 系统上webView初始化失败问题
        // https://www.twblogs.net/a/5b7f6cff2b717767c6af8a3c
        Function1<Context, WV> tryCreateWebView = context -> {
            try {
                return creator.invoke(context);
            } catch (Resources.NotFoundException ignore) {
                return null;
            }
        };
        // 尝试创建webView
        WV webView;
        try {
            webView = tryCreateWebView.invoke(placeHolder.getContext());
            // 修复 5.x 系统上webView初始化失败问题
            // https://www.twblogs.net/a/5b7f6cff2b717767c6af8a3c
            if (webView == null) {
                // https://stackoverflow.com/questions/46810118/exception-on-new-webview-on-lollipop-5-1-devices
                webView = tryCreateWebView.invoke(placeHolder.getContext().getApplicationContext());
            }
            if (webView == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                // https://codeday.me/bug/20190505/1056033.html
                webView = tryCreateWebView.invoke(placeHolder.getContext().createConfigurationContext(new Configuration()));
            }
        } catch (Throwable e) {
            Log.w(TAG, "installWebView: webView 创建失败", e);
            ToastUtil.showShort(placeHolder.getContext(), R.string.webViewLoadFailed);
            return null;
        }
        // 检查是否创建成功
        if (webView == null) {
            ToastUtil.showShort(placeHolder.getContext(), R.string.webViewLoadFailed);
            return null;
        } else {
            // 替换到placeHolder占位View
            int index = parent.indexOfChild(placeHolder);
            parent.removeViewInLayout(placeHolder);
            ViewGroup.LayoutParams layoutParams = placeHolder.getLayoutParams();
            if (layoutParams != null) {
                parent.addView(webView, index, layoutParams);
            } else {
                parent.addView(webView, index);
            }
            return webView;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void settingWebView(@NonNull WebView webView) {
        // 监听渲染器进程客户端
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_CLIENT_BASIC_USAGE)) {
            Log.i(TAG, "settingWebView: setWebViewRenderProcessClient");
            WebViewCompat.setWebViewRenderProcessClient(webView, new WebViewRenderProcessClient() {
                @Override
                public void onRenderProcessUnresponsive(@NonNull WebView view, @Nullable WebViewRenderProcess renderer) {
                    Log.i(TAG, "onRenderProcessUnresponsive: ");
                    if (renderer != null && WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_TERMINATE)) {
                        renderer.terminate();
                    } else {
                        Log.e(TAG, "onRenderProcessUnresponsive: can not terminate renderer.");
                    }
                }

                @Override
                public void onRenderProcessResponsive(@NonNull WebView view, @Nullable WebViewRenderProcess renderer) {
                    Log.i(TAG, "onRenderProcessResponsive: ");
                }
            });
        }
        WebSettings settings = webView.getSettings();
        // 安全浏览
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
            WebSettingsCompat.setSafeBrowsingEnabled(webView.getSettings(), safeBrowsingEnable);
        }
        settings.setBlockNetworkImage(false);
        // 允许使用Js
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 是否使用缓存
        settings.setAppCacheEnabled(true);
        // 如果缓存可用则从缓存中获取
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 将图片调整到适合webView的大小
        settings.setUseWideViewPort(true);
        // 支持内容重新布局
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        // 支持自动加载图片
        settings.setLoadsImagesAutomatically(true);
        // 当webView调用requestFocus时为webView设置节点
        settings.setNeedInitialFocus(true);
        // 自适应屏幕
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        // 开启DOM storage API功能（HTML5 提供的一种标准的接口，主要将键值对存储在本地，在页面加载完毕后可以通过 javascript 来操作这些数据。）
        settings.setDomStorageEnabled(true);
        // 开启数据库缓存
        settings.setDatabaseEnabled(true);
        // 支持缩放
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(true);
        settings.setTextZoom(100);

        // 允许webview对文件的操作
        settings.setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // 允许通过 file url 加载的 Javascript 读取其他的本地文件,Android 4.1 之前默认是true，在 Android 4.1 及以后默认是false,也就是禁止
            settings.setAllowFileAccessFromFileURLs(true);
            // 允许通过 file url 加载的 Javascript 可以访问其他的源，包括其他的文件和 http，https 等其他的源，
            // Android 4.1 之前默认是true，在 Android 4.1 及以后默认是false,也就是禁止
            // 如果此设置是允许，则 setAllowFileAccessFromFileURLs 不起做用
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        // WebView在5.0之前默认允许其加载混合网络协议内容
        // 在5.0之后，默认不允许加载http与https混合内容，需要设置WebView允许其加载混合网络协议内容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            Log.i(TAG, "onDownloadStart: " + url
                    + " userAgent " + userAgent
                    + " contentDisposition " + contentDisposition
                    + " mimetype " + mimetype
                    + " contentLength " + contentLength);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(url));
            webView.getContext().startActivity(intent);
        });
    }

    public static void appendScreenInfoWhenImmersive(WebView webView) {
        WebSettings settings = webView.getSettings();
        // 设置UserAgent
        settings.setUserAgentString(settings.getUserAgentString()
                + SystemUiUtil.getWebViewUserAgentSystemUiSafeAreaInsetsDescription(webView.getContext()));
    }

    public static boolean isSupportProxyOverride() {
        return WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE);
    }

    /**
     * 为webView设置代理
     * 代理地址格式为 {@code [scheme://]host[:port]}
     *
     * @param proxy 代理地址
     */
    public static void setWebViewProxy(@Nullable String proxy) {
        // 设置代理
        if (!TextUtils.isEmpty(proxy) && isSupportProxyOverride()) {
            // 标记为安全浏览已禁用
            safeBrowsingEnable = false;
            // 添加代理配置
            ProxyController.getInstance().setProxyOverride(
                    new ProxyConfig.Builder()
                            .addProxyRule(proxy)
                            .build(),
                    ExecutorUtil.INSTANCE,
                    () -> Log.i(TAG, "setWebViewProxy: success"));
        }
    }

    public static void onDestroy(@Nullable WebView webView) {
        if (webView != null) {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            ViewKt.removeSelfFromParent(webView);
            webView.destroy();
        }
    }
}
