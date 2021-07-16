package com.chenfei.base.fragment

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.github.lzyzsd.jsbridge.BridgeHandler
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.github.lzyzsd.jsbridge.WebViewJavascriptBridge
import com.github.lzyzsd.jsbridge.send
import com.chenfei.util.ExecutorUtil
import com.chenfei.util.kotlin.findOrAddChild

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-06 14:45
 */
abstract class BaseJsBridgeFragment : Fragment() {
    private var jsBridge: WebViewJavascriptBridge? = null
    private var doOnResumed: Runnable? = null

    override fun onResume() {
        super.onResume()
        // 此时fragment生命周期的状态还没有resumed
        // 为了同一化生命周期判断流程，使用postRunnable来进行执行处理回调
        doOnResumed?.let {
            ExecutorUtil.postToUiThread(it)
            doOnResumed = null
        }
    }

    protected abstract fun handler(name: String, data: String?, function: CallBackFunction)

    fun safeJsBridgeSend(
        handlerName: String, data: String?, responseCallback: CallBackFunction? = null
    ) {
        ExecutorUtil.postToUiThread {
            // 如果在前台，则转发回调
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                jsBridge?.send(handlerName, data, responseCallback)
            }
        }
    }

    companion object {

        @JvmStatic
        protected inline fun <reified F : BaseJsBridgeFragment> createBridgeHandler(
            host: FragmentHost,
            jsBridge: WebViewJavascriptBridge,
            handlerName: String,
            noinline creator: () -> F
        ) = createBridgeHandler(
            host, jsBridge, handlerName, F::class.java.name, F::class.java, creator
        )

        @JvmStatic
        protected fun <F : BaseJsBridgeFragment> createBridgeHandler(
            host: FragmentHost,
            jsBridge: WebViewJavascriptBridge,
            handlerName: String,
            tag: String,
            clazz: Class<F>,
            creator: () -> F
        ) = BridgeHandler { data, function ->
            // 获取该fragment
            val fragment = host
                .getSupportFragmentManager()
                .findOrAddChild(tag, clazz, false, creator)
                .apply {
                    // 更新其jsBridgeWebView
                    this.jsBridge = jsBridge
                }
            if (fragment.isAdded) {
                fragment.handler(handlerName, data, function)
            } else {
                fragment.doOnResumed = Runnable { fragment.handler(handlerName, data, function) }
            }
        }
    }
}
