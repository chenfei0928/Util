package io.github.chenfei0928.webkit

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.webkit.UserAgentMetadata
import androidx.webkit.WebNavigationClient
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewMediaIntegrityApiStatusConfig
import androidx.webkit.WebViewRenderProcessClient

/**
 * @author chenf()
 * @date 2025-03-24 16:07
 */
//<editor-fold desc="WebSettings属性">
var WebSettings.supportZoomCompat: Boolean
    get() = supportZoom()
    set(value) {
        setSupportZoom(value)
    }

var WebSettings.supportMultipleWindowsCompat: Boolean
    get() = supportMultipleWindows()
    set(value) {
        setSupportMultipleWindows(value)
    }
//</editor-fold>

//<editor-fold desc="WebSettingsCompat属性">
var WebSettings.openOffscreenPreRasterCompat: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        offscreenPreRaster
    } else if (WebViewFeature.isFeatureSupported(WebViewFeature.OFF_SCREEN_PRERASTER)) {
        WebSettingsCompat.getOffscreenPreRaster(this)
    } else false
    set(value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            offscreenPreRaster = value
        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.OFF_SCREEN_PRERASTER)) {
            WebSettingsCompat.setOffscreenPreRaster(this, value)
        }
    }

var WebSettings.safeBrowsingEnableCompat: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        safeBrowsingEnabled
    } else if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
        WebSettingsCompat.getSafeBrowsingEnabled(this)
    } else false
    set(value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            offscreenPreRaster = value
        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
            WebSettingsCompat.setSafeBrowsingEnabled(this, value)
        }
    }

var WebSettings.algorithmicDarkeningAllowedCompat: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        isAlgorithmicDarkeningAllowed
    } else if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
        WebSettingsCompat.isAlgorithmicDarkeningAllowed(this)
    } else false
    set(value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setAlgorithmicDarkeningAllowed(value)
        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(this, value)
        }
    }

var WebSettings.disabledActionModeMenuItemsCompat: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        disabledActionModeMenuItems
    } else if (WebViewFeature.isFeatureSupported(WebViewFeature.DISABLED_ACTION_MODE_MENU_ITEMS)) {
        WebSettingsCompat.getDisabledActionModeMenuItems(this)
    } else WebSettings.MENU_ITEM_NONE
    set(value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            disabledActionModeMenuItems = value
        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.DISABLED_ACTION_MODE_MENU_ITEMS)) {
            WebSettingsCompat.setDisabledActionModeMenuItems(this, value)
        }
    }

var WebSettings.enterpriseAuthenticationAppLinkPolicyEnabledCompat: Boolean
    get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.ENTERPRISE_AUTHENTICATION_APP_LINK_POLICY)) {
        WebSettingsCompat.getEnterpriseAuthenticationAppLinkPolicyEnabled(this)
    } else false
    set(value) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ENTERPRISE_AUTHENTICATION_APP_LINK_POLICY)) {
            WebSettingsCompat.setEnterpriseAuthenticationAppLinkPolicyEnabled(this, value)
        }
    }

var WebSettings.requestedWithHeaderOriginAllowListCompat: Set<String>
    get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
        WebSettingsCompat.getRequestedWithHeaderOriginAllowList(this)
    } else emptySet()
    set(value) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
            WebSettingsCompat.setRequestedWithHeaderOriginAllowList(this, value)
        }
    }

var WebSettings.userAgentMetadataCompat: UserAgentMetadata
    get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.USER_AGENT_METADATA)) {
        WebSettingsCompat.getUserAgentMetadata(this)
    } else WebViewConfig.USER_AGENT_METADATA
    set(value) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.USER_AGENT_METADATA)) {
            WebSettingsCompat.setUserAgentMetadata(this, value)
        }
    }

var WebSettings.attributionRegistrationBehaviorCompat: Int
    get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.ATTRIBUTION_REGISTRATION_BEHAVIOR)) {
        WebSettingsCompat.getAttributionRegistrationBehavior(this)
    } else WebSettingsCompat.ATTRIBUTION_BEHAVIOR_APP_SOURCE_AND_WEB_TRIGGER
    set(value) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ATTRIBUTION_REGISTRATION_BEHAVIOR)) {
            WebSettingsCompat.setAttributionRegistrationBehavior(this, value)
        }
    }

var WebSettings.webViewMediaIntegrityApiStatusCompat: WebViewMediaIntegrityApiStatusConfig
    get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.WEBVIEW_MEDIA_INTEGRITY_API_STATUS)) {
        WebSettingsCompat.getWebViewMediaIntegrityApiStatus(this)
    } else WebViewConfig.WEB_VIEW_MEDIA_INTEGRITY_API_STATUS
    set(value) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEBVIEW_MEDIA_INTEGRITY_API_STATUS)) {
            WebSettingsCompat.setWebViewMediaIntegrityApiStatus(this, value)
        }
    }

var WebSettings.webAuthenticationSupportCompat: Int
    get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_AUTHENTICATION)) {
        WebSettingsCompat.getWebAuthenticationSupport(this)
    } else WebSettingsCompat.WEB_AUTHENTICATION_SUPPORT_NONE
    set(value) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_AUTHENTICATION)) {
            WebSettingsCompat.setWebAuthenticationSupport(this, value)
        }
    }

@get:SuppressLint("UnsafeOptInUsageError")
@set:SuppressLint("UnsafeOptInUsageError")
var WebSettings.speculativeLoadingStatus: Int
    get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.SPECULATIVE_LOADING)) {
        WebSettingsCompat.getSpeculativeLoadingStatus(this)
    } else WebSettingsCompat.SPECULATIVE_LOADING_DISABLED
    set(value) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SPECULATIVE_LOADING)) {
            WebSettingsCompat.setSpeculativeLoadingStatus(this, value)
        }
    }

@get:SuppressLint("UnsafeOptInUsageError")
@set:SuppressLint("UnsafeOptInUsageError")
var WebSettings.backForwardCacheEnabled: Boolean
    get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.BACK_FORWARD_CACHE)) {
        WebSettingsCompat.getBackForwardCacheEnabled(this)
    } else false
    set(value) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.BACK_FORWARD_CACHE)) {
            WebSettingsCompat.setBackForwardCacheEnabled(this, value)
        }
    }

var WebSettings.paymentRequestEnabled: Boolean
    get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.PAYMENT_REQUEST)) {
        WebSettingsCompat.getPaymentRequestEnabled(this)
    } else false
    set(value) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PAYMENT_REQUEST)) {
            WebSettingsCompat.setPaymentRequestEnabled(this, value)
        }
    }

var WebSettings.hasEnrolledInstrumentEnabled: Boolean
    get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.PAYMENT_REQUEST)) {
        WebSettingsCompat.getHasEnrolledInstrumentEnabled(this)
    } else false
    set(value) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PAYMENT_REQUEST)) {
            WebSettingsCompat.setHasEnrolledInstrumentEnabled(this, value)
        }
    }
//</editor-fold>

//<editor-fold desc="WebViewCompat属性">
var WebView.webViewRenderProcessClientCompat: WebViewRenderProcessClient?
    get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_CLIENT_BASIC_USAGE)) {
        WebViewCompat.getWebViewRenderProcessClient(this)
    } else null
    set(value) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_CLIENT_BASIC_USAGE)) {
            WebViewCompat.setWebViewRenderProcessClient(this, value)
        }
    }

var WebView.isAudioMuted: Boolean
    get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.MUTE_AUDIO)) {
        WebViewCompat.isAudioMuted(this)
    } else false
    set(value) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.MUTE_AUDIO)) {
            WebViewCompat.setAudioMuted(this, value)
        }
    }

@get:SuppressLint("UnsafeOptInUsageError")
@set:SuppressLint("UnsafeOptInUsageError")
var WebView.webNavigationClient: WebNavigationClient
    get() = if (WebViewFeature.isFeatureSupported(WebViewFeature.NAVIGATION_CALLBACK_BASIC)) {
        WebViewCompat.getWebNavigationClient(this)
    } else WebViewConfig.WEB_NAVIGATION_CLIENT
    set(value) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.NAVIGATION_CALLBACK_BASIC)) {
            WebViewCompat.setWebNavigationClient(this, value)
        }
    }
//</editor-fold>
