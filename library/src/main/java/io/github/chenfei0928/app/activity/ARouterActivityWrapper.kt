package io.github.chenfei0928.app.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * 用于解决ARouter等sdk不支持由Fragment发起navigation的功能缺失问题
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-06-13 19:27
 */
@SuppressLint("Registered")
class ARouterActivityWrapper(
    private val fragment: Fragment
) : Activity() {

    // <editor-fold desc="ARouter等所用基础intent创建、跳转流程" defaultstate="collapsed">
    override fun getPackageName(): String {
        return fragment.requireContext().packageName
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        fragment.startActivityForResult(intent, requestCode, options)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        fragment.startActivityForResult(intent, requestCode)
    }

    override fun startActivity(intent: Intent, options: Bundle?) {
        fragment.startActivity(intent, options)
    }

    override fun startActivity(intent: Intent) {
        fragment.startActivity(intent)
    }

    override fun overridePendingTransition(enterAnim: Int, exitAnim: Int) {
        fragment.requireActivity().overridePendingTransition(enterAnim, exitAnim)
    }
    // </editor-fold>

    // <editor-fold desc="华为支付等sdk所用" defaultstate="collapsed">
    override fun startIntentSenderForResult(
        intent: IntentSender,
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
        intent: IntentSender,
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
    // </editor-fold>
}
