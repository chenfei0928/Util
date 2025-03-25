package io.github.chenfei0928.webkit

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.DownloadListener
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.IntRange
import androidx.collection.ArrayMap
import androidx.core.net.toUri
import androidx.webkit.UserAgentMetadata
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewMediaIntegrityApiStatusConfig
import io.github.chenfei0928.util.Log

/**
 * @author chenf()
 * @date 2025-03-24 10:18
 */
open class WebViewConfig {
    internal lateinit var webView: WebView

    //<editor-fold desc="WebSettings 字段" defaultstatus="collapsed">
    /**
     * WebView是否应该支持缩放 [WebSettings.setSupportZoom]，默认为true。
     *
     * 原始方法注释：
     *
     * 设置WebView是否应该支持使用其屏幕缩放控件和手势进行缩放。
     * 应该使用的特定缩放机制可以通过setBuiltInZoomControls来设置。
     * 此设置不影响使用 [WebView.zoomIn] 和 [WebView.zoomOut] 方法执行的缩放。
     */
    var supportZoom: Boolean = true

    /**
     * [WebSettings.setMediaPlaybackRequiresUserGesture]，默认为true。
     *
     * 原始方法注释：
     *
     * 设置WebView是否需要用户手势来播放媒体。默认为true。
     */
    var mediaPlaybackRequiresUserGesture = true

    /**
     * WebView是否应该使用其内置的缩放机制 [WebSettings.setBuiltInZoomControls]，默认为false
     *
     * 原始方法注释：
     *
     * 设置WebView是否应该使用其内置的缩放机制。
     * 内置的缩放机制包括屏幕上的缩放控件，这些控件显示在WebView的内容上，并使用捏手势来控制缩放。
     * 是否显示这些屏幕上的控件可以通过setDisplayZoomControls来设置。默认为false。
     *
     * 内置机制是当前唯一支持的缩放机制，因此建议始终启用此设置。
     * 然而，屏幕上的缩放控件在Android中是不赞成的(参见[android.widget.ZoomButtonsController])，
     * 所以建议禁用setDisplayZoomControls。
     */
    var builtInZoomControls: Boolean = false

    /**
     * WebView是否应该在屏幕上显示缩放控件 [WebSettings.setDisplayZoomControls]，默认为false
     *
     * 原始方法注释：
     *
     * 设置WebView在使用内置缩放机制时是否应该在屏幕上显示缩放控件。
     * 看到[builtInZoomControls]。默认为true。
     * 然而，屏幕上的缩放控件在Android中是不赞成的(参见[android.widget.ZoomButtonsController])所以建议把这个设为false。
     */
    var displayZoomControls: Boolean = false

    /**
     * 启用或禁用WebView中的文件访问 [WebSettings.setAllowFileAccess]，默认为false
     *
     * 原始方法注释：
     *
     * 启用或禁用WebView中的文件访问。注意，这只启用或禁用文件系统访问。
     * 资源和资源仍然可以使用 `file:///android_asset` 和 `file://android_res` 访问。
     * 注意：应用程序不应该在WebView中打开来自任何外部源的 `file://` url，
     * 如果你的应用程序接受来自外部源的任意url，不要启用此功能。
     * 建议始终使用 [androidx.webkit.WebViewAssetLoader] 通过 `http://` 方案访问文件，包括资产和资源，而不是 `file://url`。
     * 以防止可能的安全问题针对 [android.os.Build.VERSION_CODES.Q] 和更早的版本，应该显式地将此值设置为false。
     *
     * 对于目标 [android.os.Build.VERSION_CODES.Q] 及以下的应用，默认值为true。
     * 当目标为 [android.os.Build.VERSION_CODES.R] 及以上时为false。
     */
    var allowFileAccess: Boolean = false

    /**
     * [WebSettings.setAllowContentAccess] 默认值为false
     *
     * 原始方法注释：
     *
     * 启用或禁用WebView中的内容URL访问。内容URL访问允许WebView从安装在系统中的内容提供程序加载内容。默认是 true。
     */
    var allowContentAccess: Boolean = false

    /**
     * 自适应屏幕 [WebSettings.setLoadWithOverviewMode]，默认值为true
     *
     * 原始方法注释：
     *
     * 设置WebView是否以总览模式加载页面，即按宽度缩小内容以适合屏幕。
     * 当内容宽度大于WebView控件的宽度时，例如，当启用 [useWideViewPort] 时，将考虑此设置。
     * 默认为false。
     */
    var loadWithOverviewMode: Boolean = true

    /**
     * [WebSettings.setTextZoom]，默认值为100
     *
     * 原始方法注释：
     *
     * 以百分比设置页面的文本缩放。默认值是100。
     */
    var textZoom: Int = 100

    /**
     * 自适应屏幕 是否启用对viewport元标签的支持 [WebSettings.setUseWideViewPort]，默认值为true
     *
     * 原始方法注释：
     *
     * 设置WebView是否应该启用对 `viewport` HTML元标签的支持，还是应该使用一个宽的viewport。
     * 当该设置的值为false时，布局宽度总是被设置为WebView控件的宽度（以设备无关（CSS）像素为单位）。
     * 当该值为true并且页面包含 `viewport` 元标记时，将使用标记中指定的宽度值。
     * 如果页面不包含标记或不提供宽度，则将使用宽视口。
     */
    var useWideViewPort: Boolean = true

    /**
     * [WebSettings.setSupportMultipleWindows]，默认值为false
     *
     * 原始方法注释：
     *
     * 设置WebView是否支持多窗口。
     * 如果设置为true，[android.webkit.WebChromeClient.onCreateWindow] 必须由宿主应用程序实现。默认为false。
     */
    var supportMultipleWindows: Boolean = false

    /**
     * 支持内容重新布局 [WebSettings.setLayoutAlgorithm]，默认值为 NORMAL
     *
     * 原始方法注释：
     *
     * 设置底层布局算法。这将导致WebView的重新布局。默认为 [WebSettings.LayoutAlgorithm.NARROW_COLUMNS]
     */
    var layoutAlgorithm: WebSettings.LayoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL

    /**
     * 标准字体 [WebSettings.setStandardFontFamily]，默认值是 `sans-serif`
     *
     * 原始方法注释：
     *
     * 设置标准字体族名称。默认值是 `sans-serif`。
     */
    var standardFontFamily: String = "sans-serif"

    /**
     * 等宽字体 [WebSettings.setFixedFontFamily]，默认为 `monospace`
     *
     * 原始方法注释：
     *
     * 设置固定字体族名称。默认为 `monospace`。
     */
    var fixedFontFamily: String = "monospace"

    /**
     * 无衬线字体 [WebSettings.setSansSerifFontFamily]，默认值是 `sans-serif`
     *
     * 原始方法注释：
     *
     * 设置无衬线字体族名称。默认值是 `sans-serif`。
     */
    var sansSerifFontFamily: String = "sans-serif"

    /**
     * 衬线字体 [WebSettings.setSerifFontFamily]，默认值是 `sans-serif`
     *
     * 原始方法注释：
     *
     * 设置衬线字体族名称。默认值是 `sans-serif`。
     */
    var serifFontFamily: String = "sans-serif"

    /**
     * 草书字体 [WebSettings.setCursiveFontFamily]，默认为 `cursive`
     *
     * 原始方法注释：
     *
     * 设置草书字体族名称。默认为 `cursive`。
     */
    var cursiveFontFamily: String = "cursive"

    /**
     * [WebSettings.setFantasyFontFamily]，默认是 `fantasy`
     *
     * 原始方法注释：
     *
     * 设置虚拟字体族名称。默认是 `fantasy`。
     */
    var fantasyFontFamily: String = "fantasy"

    /**
     * 设置最小字体大小 [WebSettings.setMinimumFontSize]，默认是 8
     *
     * 原始方法注释：
     *
     * 设置最小字体大小。默认值是 8。
     * 1到72之间的非负整数。任何超出指定范围的数字都将被固定。
     */
    @IntRange(from = 1, to = 72)
    var minimumFontSize: Int = 8

    /**
     * 设置默认字体大小 [WebSettings.setDefaultFontSize]，默认是 16
     *
     * 原始方法注释：
     *
     * 设置默认字体大小。默认值是16。
     * 1到72之间的非负整数。任何超出指定范围的数字都将被固定。
     */
    @IntRange(from = 1, to = 72)
    var defaultFontSize: Int = 16

    /**
     * [WebSettings.setDefaultFontSize]，默认是 16
     *
     * 原始方法注释：
     *
     * 设置默认的固定字体大小。默认值是16。
     * 1到72之间的非负整数。任何超出指定范围的数字都将被固定。
     */
    @IntRange(from = 1, to = 72)
    var defaultFixedFontSize: Int = 16

    /**
     * 支持自动加载图片 [WebSettings.setLoadsImagesAutomatically]，默认为 true
     *
     * 原始方法注释：
     *
     * 设置WebView是否应该加载图像资源。
     * 注意，该方法控制所有图像的加载，包括使用数据URI方案嵌入的图像。
     * 使用 [blockNetworkImage] 来控制只加载使用网络URI方案指定的图像。
     * 请注意，如果将此设置的值从false更改为true，则WebView当前显示的内容所引用的所有图像资源都会自动加载。
     * 默认为true。
     */
    var loadsImagesAutomatically: Boolean = true

    /**
     * WebView是否不应该从网络加载图像资源 [WebSettings.setBlockNetworkImage]，默认为false
     *
     * 原始方法注释：
     *
     * 设置WebView是否不应该从网络加载图像资源（通过http和https URI方案访问的资源）。
     * 注意，除非 [loadsImagesAutomatically] 返回true，否则此方法不起作用。
     * 还要注意，使用 [blockNetworkLoads] 禁用所有网络加载也会阻止网络图像加载，即使该标志设置为false。
     * 当此设置的值从true更改为false时，WebView当前显示的内容所引用的网络图像资源将被自动获取。
     * 默认为false。
     */
    var blockNetworkImage: Boolean = false

    /**
     * WebView阻塞网络负载 [WebSettings.setBlockNetworkLoads]，默认为false
     *
     * 原始方法注释：
     *
     * 设置WebView是否不应从网络加载资源。
     * 使用setBlockNetworkImage只避免加载图像资源。
     * 请注意，如果将此设置的值从true更改为false，则WebView当前显示的内容所引用的网络资源直到 [WebView.reload] 被调用才会被获取。
     * 如果应用程序没有 [android.Manifest.permission.INTERNET] 权限，尝试设置 `false` 值将导致抛出 [SecurityException]。
     * 如果应用程序具有 [android.Manifest.permission.INTERNET] 权限，则默认为false，否则为true。
     *
     * true表示WebView阻塞网络负载
     */
    var blockNetworkLoads: Boolean = false

    /**
     * 允许使用Js [WebSettings.setJavaScriptEnabled]，默认为 true
     *
     * 原始方法注释：
     *
     * 告诉WebView启用JavaScript执行。默认为false。
     * 如果WebView应该执行JavaScript，则为true
     */
    var javaScriptEnabled: Boolean = true

    /**
     * 开启DOM storage API功能（HTML5 提供的一种标准的接口，主要将键值对存储在本地，在页面加载完毕后可以通过 javascript 来操作这些数据。）
     * [WebSettings.setDomStorageEnabled]，默认为 true
     *
     * 原始方法注释：
     *
     * 设置是否启用DOM存储API。默认值为false。
     */
    var domStorageEnabled: Boolean = true

    /**
     * 是否允许访问地理位置 [WebSettings.setGeolocationEnabled]，默认为true
     *
     * 原始方法注释：
     *
     * 设置是否启用地理位置。默认为true。
     * 请注意，为了使WebView中的页面能够使用地理定位API，必须满足以下要求：
     * 应用程序必须具有访问设备位置的权限，参见 [android.Manifest.permission.ACCESS_COARSE_LOCATION],
     * [android.Manifest.permission.ACCESS_FINE_LOCATION];
     * 应用程序必须提供 [android.webkit.WebChromeClient.onGeolocationPermissionsShowPrompt] 回调的实现，
     * 接收页面请求通过JavaScript地理定位API访问位置的通知。
     *
     * 是否启用地理定位
     */
    var geolocationEnabled: Boolean = true

    /**
     * 允许使用Js打开窗口 [WebSettings.setJavaScriptCanOpenWindowsAutomatically]，默认为true
     *
     * 原始方法注释：
     *
     * 告诉JavaScript自动打开窗口。这适用于JavaScript函数 `window.open()`。默认为false。
     * 如果JavaScript可以自动打开窗口，则为true
     */
    var javaScriptCanOpenWindowsAutomatically: Boolean = true

    /**
     * 页面文本编码 [WebSettings.setDefaultTextEncodingName]，默认为 `UTF-8`
     *
     * 原始方法注释：
     *
     * 设置解码html页面时使用的默认文本编码名称。默认为 `UTF-8`。
     */
    var defaultTextEncodingName: String = "UTF-8"

    /**
     * UserAgent [WebSettings.setUserAgentString]，默认为 null
     *
     * 原始方法注释：
     *
     * 设置WebView的用户代理字符串。如果字符串为null或空，则使用系统默认值。
     * 如果以这种方式覆盖用户代理，则用户代理客户端提示标头和导航器的值。
     * 这个WebView的userAgentData可以修改。
     * 看到 [androidx.webkit.WebSettingsCompat.setUserAgentMetadata] 的详细信息。
     * 注意，从 [android.os.Build.VERSION_CODES.KITKAT] 开始，在加载网页时更改user-agent会导致WebView再次启动加载。
     * 新的用户代理字符串
     */
    var userAgentString: String? = null

    /**
     * 当webView调用requestFocus时为webView设置节点 [WebSettings.setNeedInitialFocus]
     *
     * 原始方法注释：
     *
     * 告诉WebView是否需要设置一个节点在 [WebView.requestFocus]。默认值为true。
     */
    var needInitialFocus: Boolean = true

    /**
     * [WebSettings.setCacheMode]
     *
     * 原始方法注释：
     *
     * 覆盖缓存的使用方式。缓存的使用方式取决于导航类型。
     * 对于正常的页面加载，将检查缓存并根据需要重新验证内容。
     * 当导航返回时，不会重新验证内容，而只是从缓存中检索内容。
     *
     * 此方法允许客户端通过指定 [WebSettings.LOAD_DEFAULT]、[WebSettings.LOAD_CACHE_ELSE_NETWORK]、
     * [WebSettings.LOAD_NO_CACHE] 或 [WebSettings.LOAD_CACHE_ONLY] 中的一个来覆盖此行为。
     * 默认值为LOAD_DEFAULT。
     */
    var cacheMode: Int = WebSettings.LOAD_DEFAULT

    /**
     * [WebSettings.setMixedContentMode]
     * WebView在5.0之前默认允许其加载混合网络协议内容
     * 在5.0之后，默认不允许加载http与https混合内容，需要设置WebView允许其加载混合网络协议内容
     *
     * 原始方法注释：
     *
     * 配置WebView在安全源试图从不安全源加载资源时的行为。
     * 默认情况下，针对 [android.os.Build.VERSION_CODES.KITKAT] 或以下的应用程序默认为 [WebSettings.MIXED_CONTENT_ALWAYS_ALLOW]。
     * 针对 [android.os.Build.VERSION_CODES.LOLLIPOP] 的应用，默认为 [WebSettings.MIXED_CONTENT_NEVER_ALLOW]。
     * WebView的首选和最安全的操作模式是[WebSettings.MIXED_CONTENT_NEVER_ALLOW]，强烈建议使用 [WebSettings.MIXED_CONTENT_ALWAYS_ALLOW]。
     *
     * 要使用的混合内容模式。[WebSettings.MIXED_CONTENT_NEVER_ALLOW]， [WebSettings.MIXED_CONTENT_ALWAYS_ALLOW]
     * 或 [WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE] 之一。
     */
    var mixedContentMode: Int = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
    //</editor-fold>

    //<editor-fold desc="WebSettingsCompat 字段" defaultstatus="collapsed">
    /**
     * 离屏渲染，优化滑动时的伪影（会消耗较多内存）[WebSettingsCompat.setOffscreenPreRaster]、
     * [WebSettings.setOffscreenPreRaster]，默认值为 ![WebViewSettingsUtil.isLowRamDevice]。
     *
     * 原始方法注释：
     *
     * 设置 WebView 在屏幕外附加到窗口时是否应该光栅平铺。
     * 打开此选项可以避免在屏幕上动画WebView时呈现伪影。
     * 这种模式下的屏外 WebView 使用更多内存。默认值为false。
     * 请遵循以下指南来限制内存的使用：
     * WebView 的大小不应该大于设备屏幕的大小。
     * 将此模式的使用限制在少量的 WebView 中。将它用于可视的 WebView 和即将动画化为可视的 WebView。
     **/
    var openOffscreenPreRaster: Boolean = !WebViewSettingsUtil.isLowRamDevice

    /**
     * [WebSettingsCompat.setSafeBrowsingEnabled]、[WebSettings.setSafeBrowsingEnabled]
     *
     * 原始方法注释：
     *
     * 设置是否启用安全浏览。安全浏览允许WebView通过验证链接来防止恶意软件和网络钓鱼攻击。
     * 安全浏览可以禁用所有 WebView 使用manifest标签
     * （阅读 [一般安全浏览信息](https://developer.android.com/reference/android/webkit/WebView.html) ）。
     * manifest标记的优先级低于此API。
     * 支持安全浏览的设备默认启用安全浏览。
     */
    var safeBrowsingEnable: Boolean = WebViewSettingsUtil.safeBrowsingEnable

    /**
     * 允许使用夜间模式 [WebSettingsCompat.setAlgorithmicDarkeningAllowed]、[WebSettings.setAlgorithmicDarkeningAllowed]
     */
    var algorithmicDarkeningAllowed: Boolean = true

    /**
     * [WebSettingsCompat.setDisabledActionModeMenuItems]、[WebSettings.setDisabledActionModeMenuItems]
     *
     * 原始方法注释：
     *
     * 根据menuItems标志禁用操作模式菜单项。
     */
    var disabledActionModeMenuItems: Int = WebSettings.MENU_ITEM_NONE

    /**
     * [WebSettingsCompat.setEnterpriseAuthenticationAppLinkPolicyEnabled]
     *
     * 原始方法注释：
     *
     * 设置是否允许由管理员设置的EnterpriseAuthenticationAppLinkPolicy对WebView产生任何影响。
     * WebView中的EnterpriseAuthenticationAppLinkPolicy允许管理员指定认证url。
     * 当WebView被重定向到认证url，并且设备上的一个应用程序已经注册为该url的默认处理程序时，该应用程序就会启动。
     * 缺省情况下，启用EnterpriseAuthenticationAppLinkPolicy。
     * 有关EnterpriseAuthenticationAppLinkPolicy的更多信息，请参见
     * [此](https://source.chromium.org/chromium/chromium/src/+/main:components/policy/resources/policy_templates.json;l=32321?q=EnterpriseAuthenticationAppLinkPolicy%20file:policy_templates.json)。
     * 这个方法应该只在 [WebViewFeature.isFeatureSupported] 对于
     * [WebViewFeature.ENTERPRISE_AUTHENTICATION_APP_LINK_POLICY] 返回true时有效。
     *
     * 是否启用EnterpriseAuthenticationAppLinkPolicy。
     */
    var enterpriseAuthenticationAppLinkPolicyEnabled: Boolean = true

    /**
     * [WebSettingsCompat.setRequestedWithHeaderOriginAllowList]
     *
     * 原始方法注释：
     *
     * 设置一个允许源列表，从拥有传递 `WebSettings` 的WebView接收 `X-Requested-With` HTTP报头。
     * 从历史上看，WebView的所有请求都会发送此头，其中包含嵌入应用程序的应用程序包名称。
     * 根据已安装的WebView的版本，可能不再是这种情况，因为该头在2022年底已被弃用，并且已停止使用。
     *
     * 应用程序可以使用此方法为仍然依赖于弃用标头的服务器恢复遗留行为，
     * 但它不应该用于识别WebView到应用程序开发人员控制下的第一方服务器。
     * allow列表中字符串的格式遵循 [WebViewCompat.addWebMessageListener] 的原始规则。
     */
    var requestedWithHeaderOriginAllowList: Set<String> = emptySet()

    /**
     * [WebSettingsCompat.setUserAgentMetadata]，默认为null，即不处理
     *
     * 原始方法注释：
     *
     * 设置WebView的用户代理元数据以生成用户代理客户端提示。
     * WebView中的UserAgentMetadata用于填充用户代理客户端提示，
     * 它们可以提供客户端的品牌和版本信息，底层操作系统的品牌和主要版本，以及底层设备的详细信息。
     *
     * user-agent字符串可以通过 [WebSettings.setUserAgentString] 设置，
     * 这里是该API如何与它交互以生成用户代理客户端提示的详细信息。
     * 如果UserAgentMetadata为空，并且覆盖的user-agent包含系统默认的user-agent，则使用系统默认值。
     * 如果UserAgentMetadata为空，但是覆盖的user-agent不包含系统默认的user-agent，
     * 则只会生成[低条目的user-agent客户端提示](https://wicg.github.io/client-hints-infrastructure/#low-entropy-hint-table)。
     *
     * 有关User-Agent客户端提示的更多信息，请参阅[此处](https://wicg.github.io/ua-client-hints/)。
     * 这个方法应该只在 [WebViewFeature.isFeatureSupported] 对于 [WebViewFeature.USER_AGENT_METADATA] 返回true。
     */
    var userAgentMetadata: UserAgentMetadata = USER_AGENT_METADATA

    /**
     * [WebSettingsCompat.setAttributionRegistrationBehavior]，默认是 [WebSettingsCompat.ATTRIBUTION_BEHAVIOR_APP_SOURCE_AND_WEB_TRIGGER]
     *
     * 原始方法注释：
     *
     * 控制WebView如何与归因 [报告交互](https://developer.android.com/design-for-safety/privacy-sandbox/attribution)。
     * WebView通过允许web内容注册源和触发器来支持
     * [跨应用程序和网页](https://developer.android.com/design-for-safety/privacy-sandbox/attribution-app-to-web)
     * 的 [归因报告](https://github.com/WICG/attribution-reporting-api)。
     *
     * 默认情况下，属性源将被注册为归属于应用程序，而触发器则归属于加载的web内容（[WebSettingsCompat.ATTRIBUTION_BEHAVIOR_APP_SOURCE_AND_WEB_TRIGGER]）。
     * 只有当应用程序对如何使用WebView有特定的语义时，才需要改变这个默认值。
     * 特别是，应用内浏览器应该按照以下步骤申请
     * [allowlist来注册web源](https://developer.android.com/design-for-safety/privacy-sandbox/attribution-app-to-web#register-attribution)，
     * 然后设置 [WebSettingsCompat.ATTRIBUTION_BEHAVIOR_WEB_SOURCE_AND_WEB_TRIGGER] 行为。
     *
     * [如何应用网络资源](https://developer.android.com/design-for-safety/privacy-sandbox/attribution-app-to-web#register-attribution)
     * [WebSettingsCompat.ATTRIBUTION_BEHAVIOR_DISABLED],
     * [WebSettingsCompat.ATTRIBUTION_BEHAVIOR_APP_SOURCE_AND_WEB_TRIGGER],
     * [WebSettingsCompat.ATTRIBUTION_BEHAVIOR_WEB_SOURCE_AND_WEB_TRIGGER],
     * [WebSettingsCompat.ATTRIBUTION_BEHAVIOR_APP_SOURCE_AND_APP_TRIGGER]
     */
    var attributionRegistrationBehavior: Int =
        WebSettingsCompat.ATTRIBUTION_BEHAVIOR_APP_SOURCE_AND_WEB_TRIGGER

    /**
     * [WebSettingsCompat.setWebViewMediaIntegrityApiStatus]
     *
     * 原始方法注释：
     *
     * 设置通过 [WebViewMediaIntegrityApiStatusConfig] 提供的使用WebView Integrity API的权限。
     */
    var webViewMediaIntegrityApiStatus: WebViewMediaIntegrityApiStatusConfig =
        WEB_VIEW_MEDIA_INTEGRITY_API_STATUS

    /**
     * [WebSettingsCompat.setWebAuthenticationSupport]，默认为 [WebSettingsCompat.WEB_AUTHENTICATION_SUPPORT_NONE]
     *
     * 原始方法注释：
     *
     * 设置给定WebSettings的支持级别。
     * 这个方法应该只在 [WebViewFeature.isFeatureSupported] 对于 [WebViewFeature.WEB_AUTHENTICATION] 返回true。
     *
     * 这个WebView将使用的新支持级别。
     * 参见: [WebSettingsCompat.WEB_AUTHENTICATION_SUPPORT_NONE],
     * [WebSettingsCompat.WEB_AUTHENTICATION_SUPPORT_FOR_APP],
     * [WebSettingsCompat.WEB_AUTHENTICATION_SUPPORT_FOR_BROWSER]
     */
    var webAuthenticationSupport: Int = WebSettingsCompat.WEB_AUTHENTICATION_SUPPORT_NONE
    //</editor-fold>

    //<editor-fold desc="WebView字段" defaultstatus+collapsed">
    /**
     * [WebView.setNetworkAvailable]
     */
    var networkAvailable: Boolean = true

    /**
     * [WebView.addJavascriptInterface]、[WebView.removeJavascriptInterface]
     */
    val javascriptInterface: MutableMap<String, Any> = ArrayMap()

    /**
     * [WebView.setDownloadListener]
     */
    var downloadListener: DownloadListener =
        DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
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
    //</editor-fold>

    companion object {
        private const val TAG = "WebViewConfig"
        val USER_AGENT_METADATA: UserAgentMetadata = UserAgentMetadata.Builder().build()

        @SuppressLint("RequiresFeature")
        val WEB_VIEW_MEDIA_INTEGRITY_API_STATUS: WebViewMediaIntegrityApiStatusConfig =
            WebViewMediaIntegrityApiStatusConfig.Builder(
                WebViewMediaIntegrityApiStatusConfig.WEBVIEW_MEDIA_INTEGRITY_API_ENABLED
            ).build()
    }
}
