package io.github.chenfei0928.webkit;

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

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import androidx.webkit.WebViewRenderProcess;
import androidx.webkit.WebViewRenderProcessClient;
import io.github.chenfei0928.base.ContextProvider;
import io.github.chenfei0928.util.ExecutorUtil;
import io.github.chenfei0928.util.Log;
import io.github.chenfei0928.util.R;
import io.github.chenfei0928.util.ToastUtil;
import io.github.chenfei0928.view.SystemUiUtil;
import io.github.chenfei0928.view.ViewKt;
import kotlin.jvm.functions.Function1;

/**
 * @author MrFeng
 * @date 2017/3/8
 * @see <a href="http://reezy.me/p/20170515/android-webview/">原博客</a>
 */
public class WebViewSettingsUtil {
    private static final String TAG = "KW_WebSettingUtil";
    private static boolean safeBrowsingEnable = true;

    static {
        Context appContext = ContextProvider.Companion.getContext();
        // 当前的webView提供者
        if (WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROCESS)) {
            boolean multiProcessEnabled = WebViewCompat.isMultiProcessEnabled();
            Log.d(TAG, "WebView multiProcessEnabled: " + multiProcessEnabled);
        }
        PackageInfo packageInfo = WebViewCompat.getCurrentWebViewPackage(appContext);
        Log.d(TAG, "WebView version: " + packageInfo);
        // 安全浏览
        if (WebViewFeature.isFeatureSupported(WebViewFeature.START_SAFE_BROWSING)) {
            WebViewCompat.startSafeBrowsing(appContext, success ->
                    Log.i(TAG, "startSafeBrowsing: " + success));
        }
        // 安全浏览白名单
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ALLOWLIST)) {
            Set<String> array = new ArraySet<>(2);
            WebViewCompat.setSafeBrowsingAllowlist(array, isReceive ->
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

    /**
     * 创建并安装WebView到占位View上。
     * <p>
     * 创建WebView成功后将会将WebView替换到占位View上，并使WebView监听宿主的生命周期。
     * 以自动恢复/暂停WebView的Js计时器，并在宿主销毁时自动销毁WebView实例，以优化电量消耗与避免内存泄漏。
     *
     * @param lifecycleOwner 生命周期宿主，为WebView所在的Activity/Fragment
     * @param placeHolder    WebView布局占位View，WebView创建完毕后会将其移除
     * @param creator        WebView构造者
     */
    @Nullable
    public static <WV extends WebView> WV installWebViewWithLifecycle(
            @NonNull LifecycleOwner lifecycleOwner, @NonNull View placeHolder, @NonNull Config config, @NonNull Function1<Context, WV> creator) {
        WV webView = installWebView(placeHolder, creator);
        if (webView == null) {
            return null;
        }
        settingWebView(webView, config);
        lifecycleOwner.getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            switch (event) {
                case ON_RESUME:
                    webView.onResume();
                    webView.resumeTimers();
                    break;
                case ON_PAUSE:
                    webView.pauseTimers();
                    webView.onPause();
                    break;
                case ON_DESTROY:
                    onDestroy(webView);
                    break;
            }
        });
        return webView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void settingWebView(@NonNull WebView webView, @NonNull Config config) {
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

        // 不允许WebView对文件的操作，使用
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // 允许通过 file url 加载的 Javascript 读取其他的本地文件,Android 4.1 之前默认是true，在 Android 4.1 及以后默认是false,也就是禁止
            settings.setAllowFileAccessFromFileURLs(false);
            // 允许通过 file url 加载的 Javascript 可以访问其他的源，包括其他的文件和 http，https 等其他的源，
            // Android 4.1 之前默认是true，在 Android 4.1 及以后默认是false,也就是禁止
            // 如果此设置是允许，则 setAllowFileAccessFromFileURLs 不起做用
            settings.setAllowUniversalAccessFromFileURLs(false);
        }
        // WebView在5.0之前默认允许其加载混合网络协议内容
        // 在5.0之后，默认不允许加载http与https混合内容，需要设置WebView允许其加载混合网络协议内容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        // 夜间模式
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(settings, config.darkMode);
        }
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
            WebSettingsCompat.setForceDarkStrategy(settings, WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING);
        }
        // 离屏渲染，优化滑动时的伪影（会消耗较多内存）
        // 设置此 WebView 在屏幕外但附加到窗口时是否应光栅化图块。在屏幕上为屏幕外的 WebView 设置动画时，打开此选项可以避免渲染伪影。
        // 此模式下的屏幕外 WebView 使用更多内存。默认值为false。
        if (WebViewFeature.isFeatureSupported(WebViewFeature.OFF_SCREEN_PRERASTER)) {
            WebSettingsCompat.setOffscreenPreRaster(settings, true);
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
        if (proxy != null && !TextUtils.isEmpty(proxy) && isSupportProxyOverride()) {
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

    public static class Config {
        public int darkMode = WebSettingsCompat.FORCE_DARK_ON;
    }
}
