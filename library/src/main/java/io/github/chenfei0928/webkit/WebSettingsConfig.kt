package io.github.chenfei0928.webkit

import android.webkit.WebSettings
import android.webkit.WebView
import kotlin.reflect.KMutableProperty1

/**
 * @author chenf()
 * @date 2025-03-24 17:31
 */
internal interface WebSettingsConfig<T, V> {
    operator fun set(t: T, value: V)
    val WebViewConfig.value: V

    fun apply(settings: T, config: WebViewConfig) {
        set(settings, config.value)
    }

    interface Gettable<T, V> : WebSettingsConfig<T, V> {
        operator fun get(t: T): V

        override fun apply(settings: T, config: WebViewConfig) {
            val value = config.value
            if (get(settings) != value) {
                set(settings, value)
            }
        }
    }

    companion object {
        inline operator fun <T, V> invoke(
            crossinline get: (T) -> V,
            crossinline set: (T, V) -> Unit,
            crossinline configGet: (WebViewConfig) -> V
        ) = object : Gettable<T, V> {
            override fun get(t: T): V = get(t)
            override fun set(t: T, value: V) = set(t, value)
            override val WebViewConfig.value: V
                get() = configGet(this)
        }

        inline operator fun <T, V> invoke(
            property: KMutableProperty1<T, V>,
            crossinline configGet: (WebViewConfig) -> V
        ) = object : Gettable<T, V> {
            override fun get(t: T): V = property.get(t)
            override fun set(t: T, value: V) = property.set(t, value)
            override val WebViewConfig.value: V
                get() = configGet(this)
        }

        inline fun <T, V> writeOnly(
            crossinline set: (T, V) -> Unit,
            crossinline configGet: (WebViewConfig) -> V
        ) = object : WebSettingsConfig<T, V> {
            override fun set(t: T, value: V) = set(t, value)
            override val WebViewConfig.value: V
                get() = configGet(this)
        }

        val webViewConfigField: Array<WebSettingsConfig<WebView, *>> = arrayOf(
            invoke(
                WebView::isAudioMuted,
                WebViewConfig::isAudioMuted
            ),
        )

        val settingsConfigField: Array<WebSettingsConfig<WebSettings, *>> = arrayOf(
            invoke(WebSettings::supportZoomCompat, WebViewConfig::supportZoom),
            invoke(
                WebSettings::getMediaPlaybackRequiresUserGesture,
                WebSettings::setMediaPlaybackRequiresUserGesture,
                WebViewConfig::mediaPlaybackRequiresUserGesture
            ),
            invoke(
                WebSettings::getBuiltInZoomControls,
                WebSettings::setBuiltInZoomControls,
                WebViewConfig::builtInZoomControls
            ),
            invoke(
                WebSettings::getDisplayZoomControls,
                WebSettings::setDisplayZoomControls,
                WebViewConfig::displayZoomControls
            ),
            invoke(
                WebSettings::getAllowFileAccess,
                WebSettings::setAllowFileAccess,
                WebViewConfig::allowFileAccess
            ),
            invoke(
                WebSettings::getAllowContentAccess,
                WebSettings::setAllowContentAccess,
                WebViewConfig::allowContentAccess
            ),
            invoke(
                WebSettings::getLoadWithOverviewMode,
                WebSettings::setLoadWithOverviewMode,
                WebViewConfig::loadWithOverviewMode
            ),
            invoke(
                WebSettings::getTextZoom,
                WebSettings::setTextZoom,
                WebViewConfig::textZoom
            ),
            invoke(
                WebSettings::getUseWideViewPort,
                WebSettings::setUseWideViewPort,
                WebViewConfig::useWideViewPort
            ),
            invoke(
                WebSettings::supportMultipleWindowsCompat,
                WebViewConfig::supportMultipleWindows
            ),
            invoke(
                WebSettings::getLayoutAlgorithm,
                WebSettings::setLayoutAlgorithm,
                WebViewConfig::layoutAlgorithm
            ),
            invoke(
                WebSettings::getStandardFontFamily,
                WebSettings::setStandardFontFamily,
                WebViewConfig::standardFontFamily
            ),
            invoke(
                WebSettings::getFixedFontFamily,
                WebSettings::setFixedFontFamily,
                WebViewConfig::fixedFontFamily
            ),
            invoke(
                WebSettings::getSansSerifFontFamily,
                WebSettings::setSansSerifFontFamily,
                WebViewConfig::sansSerifFontFamily
            ),
            invoke(
                WebSettings::getSerifFontFamily,
                WebSettings::setSerifFontFamily,
                WebViewConfig::serifFontFamily
            ),
            invoke(
                WebSettings::getCursiveFontFamily,
                WebSettings::setCursiveFontFamily,
                WebViewConfig::cursiveFontFamily
            ),
            invoke(
                WebSettings::getFantasyFontFamily,
                WebSettings::setFantasyFontFamily,
                WebViewConfig::fantasyFontFamily
            ),
            invoke(
                WebSettings::getMinimumFontSize,
                WebSettings::setMinimumFontSize,
                WebViewConfig::minimumFontSize
            ),
            invoke(
                WebSettings::getDefaultFontSize,
                WebSettings::setDefaultFontSize,
                WebViewConfig::defaultFontSize
            ),
            invoke(
                WebSettings::getDefaultFixedFontSize,
                WebSettings::setDefaultFixedFontSize,
                WebViewConfig::defaultFixedFontSize
            ),
            invoke(
                WebSettings::getLoadsImagesAutomatically,
                WebSettings::setLoadsImagesAutomatically,
                WebViewConfig::loadsImagesAutomatically
            ),
            invoke(
                WebSettings::getBlockNetworkImage,
                WebSettings::setBlockNetworkImage,
                WebViewConfig::blockNetworkImage
            ),
            invoke(
                WebSettings::getBlockNetworkLoads,
                WebSettings::setBlockNetworkLoads,
                WebViewConfig::blockNetworkLoads
            ),
            invoke(
                WebSettings::getJavaScriptEnabled,
                WebSettings::setJavaScriptEnabled,
                WebViewConfig::javaScriptEnabled
            ),
            invoke(
                WebSettings::getDomStorageEnabled,
                WebSettings::setDomStorageEnabled,
                WebViewConfig::domStorageEnabled
            ),
            writeOnly(
                WebSettings::setGeolocationEnabled,
                WebViewConfig::geolocationEnabled
            ),
            invoke(
                WebSettings::getJavaScriptCanOpenWindowsAutomatically,
                WebSettings::setJavaScriptCanOpenWindowsAutomatically,
                WebViewConfig::javaScriptCanOpenWindowsAutomatically
            ),
            invoke(
                WebSettings::getDefaultTextEncodingName,
                WebSettings::setDefaultTextEncodingName,
                WebViewConfig::defaultTextEncodingName
            ),
            invoke(
                WebSettings::getUserAgentString,
                WebSettings::setUserAgentString,
                WebViewConfig::userAgentString
            ),
            writeOnly(
                WebSettings::setNeedInitialFocus,
                WebViewConfig::needInitialFocus
            ),
            invoke(
                WebSettings::getCacheMode,
                WebSettings::setCacheMode,
                WebViewConfig::cacheMode
            ),
            invoke(
                WebSettings::getMixedContentMode,
                WebSettings::setMixedContentMode,
                WebViewConfig::mixedContentMode
            ),
            invoke(
                WebSettings::openOffscreenPreRasterCompat,
                WebViewConfig::openOffscreenPreRaster
            ),
            invoke(
                WebSettings::safeBrowsingEnableCompat,
                WebViewConfig::safeBrowsingEnable
            ),
            invoke(
                WebSettings::algorithmicDarkeningAllowedCompat,
                WebViewConfig::algorithmicDarkeningAllowed
            ),
            invoke(
                WebSettings::disabledActionModeMenuItemsCompat,
                WebViewConfig::disabledActionModeMenuItems
            ),
            invoke(
                WebSettings::enterpriseAuthenticationAppLinkPolicyEnabledCompat,
                WebViewConfig::enterpriseAuthenticationAppLinkPolicyEnabled
            ),
            invoke(
                WebSettings::requestedWithHeaderOriginAllowListCompat,
                WebViewConfig::requestedWithHeaderOriginAllowList
            ),
            invoke(
                WebSettings::userAgentMetadataCompat,
                WebViewConfig::userAgentMetadata
            ),
            invoke(
                WebSettings::attributionRegistrationBehaviorCompat,
                WebViewConfig::attributionRegistrationBehavior
            ),
            invoke(
                WebSettings::webViewMediaIntegrityApiStatusCompat,
                WebViewConfig::webViewMediaIntegrityApiStatus
            ),
            invoke(
                WebSettings::webAuthenticationSupportCompat,
                WebViewConfig::webAuthenticationSupport
            ),
        )
    }
}
