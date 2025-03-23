package io.github.chenfei0928.webkit

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.collection.ArraySet
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.webkit.ProcessGlobalConfig
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewRenderProcess
import androidx.webkit.WebViewRenderProcessClient
import io.github.chenfei0928.app.ProcessUtil
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.concurrent.FragileBooleanDelegate
import io.github.chenfei0928.content.findActivity
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.util.Log
import io.github.chenfei0928.util.R
import io.github.chenfei0928.view.SystemUiUtil
import io.github.chenfei0928.view.findParentFragment
import io.github.chenfei0928.widget.ToastUtil

/**
 * WebView设置工具
 *
 * [原博客](http://reezy.me/p/20170515/android-webview/)
 *
 * @author MrFeng
 * @date 2017/3/8
 */
object WebViewSettingsUtil {
    private const val TAG = "KW_WebSettingUtil"

    //<editor-fold desc="初始化环境" defaultstatus="collapsed">
    private var safeBrowsingEnable = true
    private val initEnvironment by FragileBooleanDelegate()
    private var isLowRamDevice = false

    /**
     * 为webView设置代理
     * 代理地址格式为 `[scheme://]host[:port]`
     *
     * @param proxy 代理地址
     */
    fun setWebViewProxy(proxy: String?) {
        // 设置代理
        if (!proxy.isNullOrBlank() && WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            // 标记为安全浏览已禁用
            safeBrowsingEnable = false
            // 添加代理配置
            ProxyController.getInstance().setProxyOverride(
                ProxyConfig.Builder().addProxyRule(proxy).build(), ExecutorUtil
            ) { Log.d(TAG, "setWebViewProxy: success") }
        }
    }

    fun initEnvironment(context: Context) {
        if (!initEnvironment) {
            return
        }
        val appContext = context.applicationContext
        // 在实例构建前设置目录前缀
        /**
         * webView隔离，隔离不同进程webView的数据目录
         * 同一个进程中，只允许在webView初始化前调用，webView创建一次后调用会导致设置失败并activity退出
         *
         * [官方介绍](https://developer.android.google.cn/about/versions/pie/android-9.0-changes-28.web-data-dirs)
         *
         * [相关博文](https://www.sunzn.com/2019/04/18/Android-P-%E8%A1%8C%E4%B8%BA%E5%8F%98%E6%9B%B4%E5%AF%B9-WebView-%E7%9A%84%E5%BD%B1%E5%93%8D/)
         */
        if (WebViewFeature.isStartupFeatureSupported(
                appContext, WebViewFeature.STARTUP_FEATURE_SET_DATA_DIRECTORY_SUFFIX
            )
        ) {
            val config = ProcessGlobalConfig()
            val processName = ProcessUtil.getProcessName(appContext)
            // 如果运行的进程不是主进程中，添加前缀
            if (appContext.packageName != processName) {
                config.setDataDirectorySuffix(appContext, processName)
            }
            Debug.countTime(TAG, "checkWebViewDir:") {
                ProcessGlobalConfig.apply(config)
            }
        }
        isLowRamDevice = appContext.getSystemService<ActivityManager>()?.isLowRamDevice == true
        // 当前的webView提供者
        if (WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROCESS)) {
            val multiProcessEnabled = WebViewCompat.isMultiProcessEnabled()
            Log.d(TAG, "WebView multiProcessEnabled: $multiProcessEnabled")
        }
        val packageInfo = WebViewCompat.getCurrentWebViewPackage(appContext)
        Log.d(TAG, "WebView version: $packageInfo")
        // 安全浏览
        if (WebViewFeature.isFeatureSupported(WebViewFeature.START_SAFE_BROWSING)) {
            WebViewCompat.startSafeBrowsing(appContext) { success: Boolean ->
                Log.i(TAG, "startSafeBrowsing: $success")
            }
        }
        // 安全浏览白名单
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ALLOWLIST)) {
            val array: Set<String> = ArraySet(2)
            WebViewCompat.setSafeBrowsingAllowlist(array) { isReceive: Boolean ->
                Log.i(TAG, "setSafeBrowsingWhitelist: $isReceive")
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="构建实例与销毁实例" defaultstatus="collapsed">
    /**
     * 构建并安装实例
     *
     * @param placeHolder webView的placeHolder
     * @param creator 构建webView实例的回调
     * @return 构建成功的webView，或null
     */
    fun <V : WebView> installWebView(
        placeHolder: View,
        parentView: ViewGroup = placeHolder.parent as ViewGroup,
        creator: (Context) -> V
    ): V? {
        // 在构建实例前初始化环境
        initEnvironment(placeHolder.context)
        // 修复 5.x 系统上webView初始化失败问题
        // https://www.twblogs.net/a/5b7f6cff2b717767c6af8a3c
        val tryCreateWebView: Function1<Context, V?> = label@{ context: Context ->
            return@label try {
                creator.invoke(context)
            } catch (_: Resources.NotFoundException) {
                null
            }
        }
        // 尝试创建webView
        val webView = try {
            tryCreateWebView.invoke(placeHolder.context)
                ?:
                // 修复 5.x 系统上webView初始化失败问题
                // https://www.twblogs.net/a/5b7f6cff2b717767c6af8a3c
                // https://stackoverflow.com/questions/46810118/exception-on-new-webview-on-lollipop-5-1-devices
                tryCreateWebView.invoke(placeHolder.context.applicationContext)
                ?:
                // https://codeday.me/bug/20190505/1056033.html
                tryCreateWebView.invoke(placeHolder.context.createConfigurationContext(Configuration()))
        } catch (e: Throwable) {
            // 因为其他原因加载WebView失败（如安装了错误abi版本的WebView内核）
            Log.w(TAG, "installWebView: webView 创建失败", e)
            ToastUtil.showShort(placeHolder.context, R.string.cf0928util_webViewLoadFailed)
            return null
        }
        // 检查是否创建成功
        return if (webView == null) {
            ToastUtil.showShort(placeHolder.context, R.string.cf0928util_webViewLoadFailed)
            null
        } else webView.apply {
            // 更新其id，以兼容相对布局/约束布局依赖viewId布局ui的父View
            id = placeHolder.id
            // 替换到placeHolder占位View
            val index = parentView.indexOfChild(placeHolder)
            parentView.removeView(placeHolder)
            val layoutParams = placeHolder.layoutParams
            if (layoutParams != null) {
                parentView.addView(this, index, layoutParams)
            } else {
                parentView.addView(this, index)
            }
        }
    }

    /**
     * 创建并安装WebView到占位View上。
     *
     * 创建WebView成功后将会将WebView替换到占位View上，并使WebView监听宿主的生命周期。
     * 以自动恢复/暂停WebView的Js计时器，并在宿主销毁时自动销毁WebView实例，以优化电量消耗与避免内存泄漏。
     *
     * @param config WebView构造配置
     */
    fun <V : WebView> installWebViewWithLifecycle(
        config: ConfigWithCreator<V>,
    ): V? = installWebViewWithLifecycleImpl(config)

    internal fun <V : WebView> installWebViewWithLifecycleImpl(
        config: ConfigWithCreator<V>,
    ): V? {
        val webView = installWebView(config.placeHolder, config.parentView, config::create)
            ?: return null
        val lifecycleObserver = WebViewLifecycleOwner(webView, config.placeHolder)
        config.lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        settingWebView(webView, lifecycleObserver, config)
        config.onWebViewCreated(webView, lifecycleObserver)
        return webView
    }

    fun onDestroy(webView: WebView?, placeHolder: View? = null) {
        if (webView != null) {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            webView.clearHistory()
            webView.webViewClient = WebViewClient()
            webView.webChromeClient = null
            (webView.parent as? ViewGroup)?.let { parentView ->
                val index = parentView.indexOfChild(webView)
                parentView.removeView(webView)
                val layoutParams = webView.layoutParams
                if (layoutParams != null) {
                    parentView.addView(placeHolder, index, layoutParams)
                } else {
                    parentView.addView(placeHolder, index)
                }
            }
            webView.destroy()
        }
    }
    //</editor-fold>

    //<editor-fold desc="设置构建完成的WebView实例" defaultstatus="collapsed">
    @SuppressLint("SetJavaScriptEnabled")
    private fun settingWebViewByConfigBase(webView: WebView, config: Config) {
        val settings = webView.settings
        // 安全浏览
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
            WebSettingsCompat.setSafeBrowsingEnabled(settings, safeBrowsingEnable)
        }
        settings.blockNetworkImage = false
        // 允许使用Js
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        // 如果缓存可用则从缓存中获取
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        // 将图片调整到适合webView的大小
        settings.useWideViewPort = true
        // 支持内容重新布局
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        // 支持自动加载图片
        settings.loadsImagesAutomatically = true
        // 当webView调用requestFocus时为webView设置节点
        settings.setNeedInitialFocus(true)
        // 自适应屏幕
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        // 开启DOM storage API功能（HTML5 提供的一种标准的接口，主要将键值对存储在本地，在页面加载完毕后可以通过 javascript 来操作这些数据。）
        settings.domStorageEnabled = true
        // 支持缩放
        settings.builtInZoomControls = false
        settings.setSupportZoom(true)

        // 不允许WebView对文件的操作，使用
        settings.allowFileAccess = false
        settings.allowContentAccess = false
        // WebView在5.0之前默认允许其加载混合网络协议内容
        // 在5.0之后，默认不允许加载http与https混合内容，需要设置WebView允许其加载混合网络协议内容
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        // 夜间模式
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(
                settings, config.algorithmicDarkeningAllowed
            )
        }
        // 离屏渲染，优化滑动时的伪影（会消耗较多内存）
        // 设置此 WebView 在屏幕外但附加到窗口时是否应光栅化图块。在屏幕上为屏幕外的 WebView 设置动画时，打开此选项可以避免渲染伪影。
        // 此模式下的屏幕外 WebView 使用更多内存。默认值为false。
        if (WebViewFeature.isFeatureSupported(WebViewFeature.OFF_SCREEN_PRERASTER)) {
            WebSettingsCompat.setOffscreenPreRaster(settings, config.openOffscreenPreRaster)
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            Log.v(
                TAG, "onDownloadStart: " + url
                        + " userAgent " + userAgent
                        + " contentDisposition " + contentDisposition
                        + " mimetype " + mimetype
                        + " contentLength " + contentLength
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.setData(url.toUri())
            webView.context.startActivity(intent)
        }
    }

    fun settingWebView(webView: WebView, config: Config) {
        settingWebViewByConfigBase(webView, config)
        // 监听渲染器进程客户端
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_CLIENT_BASIC_USAGE)) {
            Log.i(TAG, "settingWebView: setWebViewRenderProcessClient")
            WebViewCompat.setWebViewRenderProcessClient(webView, BaseWebViewRenderProcessClient())
        }
    }

    fun <V : WebView> settingWebView(
        webView: V,
        observer: WebViewLifecycleOwner<V>,
        config: ConfigWithCreator<V>
    ) {
        settingWebViewByConfigBase(webView, config)
        // 监听渲染器进程客户端
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_CLIENT_BASIC_USAGE)) {
            Log.i(TAG, "settingWebView: setWebViewRenderProcessClient")
            WebViewCompat.setWebViewRenderProcessClient(
                webView, ConfigWebViewRenderProcessClient(config)
            )
        }

        // 设置webView的client
        webView.webChromeClient = config.webChromeClient
        webView.webViewClient = if (
            config.restartWebViewOnRenderGone == ConfigWithCreator.RENDER_GONE_NOOP
        ) {
            config.webViewClient
        } else {
            WebViewClientWrapper.RestartWhenRenderProcessGone(
                config.webViewClient, observer, config
            )
        }
    }

    fun appendScreenInfoWhenImmersive(webView: WebView) {
        val settings = webView.settings
        // 设置UserAgent
        settings.userAgentString += SystemUiUtil.getWebViewUserAgentSystemUiSafeAreaInsetsDescription(
            webView.context
        )
    }

    private open class BaseWebViewRenderProcessClient : WebViewRenderProcessClient() {
        override fun onRenderProcessUnresponsive(view: WebView, renderer: WebViewRenderProcess?) {
            Log.e(TAG, "onRenderProcessUnresponsive: can not terminate renderer.")
        }

        override fun onRenderProcessResponsive(view: WebView, renderer: WebViewRenderProcess?) {
            Log.v(TAG, "onRenderProcessResponsive: ")
        }
    }

    private class ConfigWebViewRenderProcessClient(
        private val config: ConfigWithCreator<out WebView>,
    ) : BaseWebViewRenderProcessClient() {
        override fun onRenderProcessUnresponsive(view: WebView, renderer: WebViewRenderProcess?) {
            if (!config.restartRenderProcessWhenUnresponsive) {
                Log.w(TAG, "onRenderProcessUnresponsive: $renderer but noop")
            } else if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_TERMINATE)) {
                Log.w(TAG, run {
                    "onRenderProcessUnresponsive: $renderer, will be restart render process."
                })
                renderer?.terminate()
            } else {
                Log.e(TAG, "onRenderProcessUnresponsive: can not terminate renderer.")
            }
        }
    }
    //</editor-fold>

    open class Config(
        /**
         * 允许使用夜间模式
         */
        var algorithmicDarkeningAllowed: Boolean = true,
        /**
         * 离屏渲染，优化滑动时的伪影（会消耗较多内存）
         * 设置此 WebView 在屏幕外但附加到窗口时是否应光栅化图块。
         * 在屏幕上为屏幕外的 WebView 设置动画时，打开此选项可以避免渲染伪影。
         * 此模式下的屏幕外 WebView 使用更多内存。默认值为 ![WebViewSettingsUtil.isLowRamDevice]。
         **/
        var openOffscreenPreRaster: Boolean = !isLowRamDevice,
    )

    @Suppress("LongParameterList")
    abstract class ConfigWithCreator<V : WebView>(
        val lifecycleOwner: LifecycleOwner,
        val placeHolder: View,
        /**
         * 允许使用夜间模式
         */
        algorithmicDarkeningAllowed: Boolean = true,
        /**
         * 离屏渲染，优化滑动时的伪影（会消耗较多内存）
         * 设置此 WebView 在屏幕外但附加到窗口时是否应光栅化图块。
         * 在屏幕上为屏幕外的 WebView 设置动画时，打开此选项可以避免渲染伪影。
         * 此模式下的屏幕外 WebView 使用更多内存。默认值为 ![WebViewSettingsUtil.isLowRamDevice]。
         **/
        openOffscreenPreRaster: Boolean = !isLowRamDevice,
        /**
         * 当 WebView 实例的渲染进程丢失时，自动重启 WebView
         *
         * 但需要应用当前进程内所有 WebView 都实现了自动重启的实现
         */
        var restartWebViewOnRenderGone: Int = RENDER_GONE_RECREATE_VIEW,
        /**
         * 如果为true时，当渲染进程无响应时重启渲染进程
         */
        var restartRenderProcessWhenUnresponsive: Boolean = true,
        var webViewClient: WebViewClient = BaseWebViewClient(placeHolder.context),
        var webChromeClient: WebChromeClient = BaseWebChromeClient(),
    ) : Config(algorithmicDarkeningAllowed, openOffscreenPreRaster) {
        val parentView: ViewGroup = placeHolder.parent as ViewGroup
        var hostActivity: Activity? = null
            get() = field ?: parentView.context.findActivity()
        var hostFragment: Fragment? = null
            get() = field ?: parentView.findParentFragment()

        abstract fun create(context: Context): V
        abstract fun onWebViewCreated(webView: V, webViewLifecycleOwner: LifecycleOwner)

        companion object {
            const val RENDER_GONE_NOOP = 0
            const val RENDER_GONE_CRASH = 1
            const val RENDER_GONE_RECREATE_ACTIVITY = 2
            const val RENDER_GONE_REUSE_HOST_FRAGMENT = 3
            const val RENDER_GONE_RECREATE_HOST_FRAGMENT = 4
            const val RENDER_GONE_RECREATE_VIEW = 5

            inline operator fun <V : WebView> invoke(
                hostFragment: Fragment,
                placeHolder: View,
                crossinline create: (Context) -> V,
                crossinline onWebViewCreated: (webView: V, webViewLifecycleOwner: LifecycleOwner) -> Unit
            ) = object : ConfigWithCreator<V>(hostFragment.viewLifecycleOwner, placeHolder) {
                init {
                    this.hostActivity = hostFragment.requireActivity()
                    this.hostFragment = hostFragment
                }

                override fun create(context: Context): V = create(context)

                override fun onWebViewCreated(webView: V, webViewLifecycleOwner: LifecycleOwner) =
                    onWebViewCreated(webView, webViewLifecycleOwner)
            }
        }
    }
}
