package io.github.chenfei0928.base.fragment

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.github.lzyzsd.jsbridge.BridgeHandler
import com.github.lzyzsd.jsbridge.BridgeWebView
import com.github.lzyzsd.jsbridge.OnBridgeCallback
import io.github.chenfei0928.app.fragment.findOrAddChild
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.util.Log
import io.github.chenfei0928.webkit.BaseLogWebViewClient

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-06 14:45
 */
abstract class BaseJsBridgeFragment : Fragment() {
    private var jsBridge: BridgeWebView? = null
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

    protected abstract fun handler(name: String, data: String?, function: OnBridgeCallback)

    protected fun safeJsBridgeSend(
        handlerName: String, data: String?, responseCallback: OnBridgeCallback? = null
    ) {
        ExecutorUtil.postToUiThread {
            if (BaseLogWebViewClient.debugLog) {
                Log.v(TAG, "send: $handlerName $data")
            }
            // 如果在前台，则转发回调
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                jsBridge?.callHandler(handlerName, data, responseCallback)
            }
        }
    }

    companion object {
        private const val TAG = "KW_BaseJsBridgeFragment"

        @JvmStatic
        protected inline fun <reified F : BaseJsBridgeFragment> createBridgeHandler(
            host: FragmentHost,
            jsBridge: BridgeWebView,
            handlerName: String,
            noinline creator: () -> F
        ) = createBridgeHandler(
            host, jsBridge, handlerName, F::class.java.name, F::class.java, creator
        )

        @JvmStatic
        protected fun <F : BaseJsBridgeFragment> createBridgeHandler(
            host: FragmentHost,
            jsBridge: BridgeWebView,
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
