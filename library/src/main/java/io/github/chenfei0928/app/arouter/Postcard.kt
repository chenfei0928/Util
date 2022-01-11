/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-11 11:10
 */
package io.github.chenfei0928.app.arouter

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import io.github.chenfei0928.app.activity.ARouterActivityWrapper

fun Postcard.navigation(fragment: Fragment, requestCode: Int) {
    navigation(ARouterActivityWrapper(fragment), requestCode)
}

fun Postcard.navigation(fragment: Fragment, requestCode: Int, callback: NavigationCallback?) {
    navigation(ARouterActivityWrapper(fragment), requestCode, callback)
}

inline fun Postcard.navigation(
    fragment: Fragment,
    crossinline onArrivalAction: (Postcard) -> Unit
) {
    navigation(fragment, -1, onArrivalAction)
}

inline fun Postcard.navigation(
    fragment: Fragment,
    requestCode: Int,
    crossinline onArrivalAction: (Postcard) -> Unit
) {
    navigation(fragment, requestCode, object : NavigationCallback {
        override fun onLost(postcard: Postcard) {
            // 路由不匹配
        }

        override fun onFound(postcard: Postcard) {
            // 导航被创建
        }

        override fun onInterrupt(postcard: Postcard) {
            // 被拦截
        }

        override fun onArrival(postcard: Postcard) {
            // 到达
            onArrivalAction(postcard)
        }
    })
}
