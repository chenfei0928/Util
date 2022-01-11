package io.github.chenfei0928.app.arouter

import android.app.Activity
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.alibaba.android.arouter.launcher.ARouter

inline fun <reified T> ARouter.navigation(): T {
    return navigation(T::class.java)
}

inline fun Postcard.navigation(context: Activity, crossinline onArrivalAction: (Postcard) -> Unit) {
    navigation(context, -1, onArrivalAction)
}

inline fun Postcard.navigation(
    context: Activity, requestCode: Int = -1, crossinline onArrivalAction: (Postcard) -> Unit
) {
    navigation(context, requestCode, object : NavigationCallback {
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
