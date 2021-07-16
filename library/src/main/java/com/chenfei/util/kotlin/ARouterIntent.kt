package com.chenfei.util.kotlin

import android.content.Intent
import androidx.core.net.toUri
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.launcher.ARouter

fun Intent.getIntExtraOrQueryParameter(name: String): Int? {
    return if (hasExtra(name)) {
        getIntExtra(name, -1)
    } else {
        getARouterUriQueryParameter(name)
            ?.toIntOrNull()
    }
}

fun Intent.getStringExtraOrQueryParameter(name: String): String? {
    return if (hasExtra(name)) {
        getStringExtra(name)
    } else {
        getARouterUriQueryParameter(name)
    }
}

fun Intent.getARouterUriQueryParameter(name: String): String? =
    aRouterRawUri?.toUri()?.getQueryParameter(name)
        ?: data?.getQueryParameter(name)

/**
 * 用于获取[Postcard.uri]方式设置进去的Uri
 */
val Intent.aRouterRawUri: String?
    get() = getStringExtra(ARouter.RAW_URI)
