/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-05 17:40
 */
package com.github.lzyzsd.jsbridge

import io.github.chenfei0928.util.Log


private const val TAG = "KW_BridgeWebView"
const val YY_OVERRIDE_SCHEMA = BridgeUtil.YY_OVERRIDE_SCHEMA

fun WebViewJavascriptBridge.send(
    handlerName: String,
    data: String?,
    responseCallback: CallBackFunction? = null
) {
    Log.v(TAG, "send: $handlerName $data")
    when (this) {
        is BridgeWebView -> {
            callHandler(handlerName, data, responseCallback)
        }
        else -> {
            Log.w(TAG, "send: $this")
        }
    }
}

object LogDefaultHandler : BridgeHandler {
    override fun handler(data: String?, function: CallBackFunction?) {
        Log.i(TAG, "handler: $data")
        function?.onCallBack("DefaultHandler response data")
    }
}
