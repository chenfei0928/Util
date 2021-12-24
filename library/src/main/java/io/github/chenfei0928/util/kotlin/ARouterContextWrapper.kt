package io.github.chenfei0928.util.kotlin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback

/**
 * 用于解决ARouter不支持由Fragment发起navigation的功能缺失问题
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-06-13 19:27
 */
@SuppressLint("Registered")
private class ARouterActivityWrapper(
        private val fragment: Fragment
) : Activity() {
    override fun getPackageName(): String {
        return fragment.requireContext().packageName
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        fragment.startActivityForResult(intent, requestCode, options)
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        fragment.startActivityForResult(intent, requestCode)
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        fragment.startActivity(intent, options)
    }

    override fun startActivity(intent: Intent?) {
        fragment.startActivity(intent)
    }

    override fun overridePendingTransition(enterAnim: Int, exitAnim: Int) {
        fragment.requireActivity().overridePendingTransition(enterAnim, exitAnim)
    }

    override fun startIntentSenderForResult(
        intent: IntentSender?,
        requestCode: Int,
        fillInIntent: Intent?,
        flagsMask: Int,
        flagsValues: Int,
        extraFlags: Int
    ) {
        fragment.startIntentSenderForResult(
            intent,
            requestCode,
            fillInIntent,
            flagsMask,
            flagsValues,
            extraFlags,
            null
        )
    }

    override fun startIntentSenderForResult(
        intent: IntentSender?,
        requestCode: Int,
        fillInIntent: Intent?,
        flagsMask: Int,
        flagsValues: Int,
        extraFlags: Int,
        options: Bundle?
    ) {
        fragment.startIntentSenderForResult(
            intent,
            requestCode,
            fillInIntent,
            flagsMask,
            flagsValues,
            extraFlags,
            options
        )
    }

    override fun startIntentSenderFromChild(
        child: Activity?,
        intent: IntentSender?,
        requestCode: Int,
        fillInIntent: Intent?,
        flagsMask: Int,
        flagsValues: Int,
        extraFlags: Int
    ) {
        TODO()
    }

    override fun startIntentSenderFromChild(
        child: Activity?,
        intent: IntentSender?,
        requestCode: Int,
        fillInIntent: Intent?,
        flagsMask: Int,
        flagsValues: Int,
        extraFlags: Int,
        options: Bundle?
    ) {
        TODO()
    }
}

fun Postcard.navigation(fragment: Fragment, requestCode: Int) {
    navigation(ARouterActivityWrapper(fragment), requestCode)
}

fun Postcard.navigation(fragment: Fragment, requestCode: Int, callback: NavigationCallback?) {
    navigation(ARouterActivityWrapper(fragment), requestCode, callback)
}

inline fun Postcard.navigation(fragment: Fragment, crossinline onArrivalAction: (Postcard) -> Unit) {
    navigation(fragment, -1, onArrivalAction)
}

inline fun Postcard.navigation(fragment: Fragment, requestCode: Int, crossinline onArrivalAction: (Postcard) -> Unit) {
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
