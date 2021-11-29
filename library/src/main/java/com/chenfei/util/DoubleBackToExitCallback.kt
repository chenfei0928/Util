package com.chenfei.util

import android.content.Context
import android.os.Handler
import android.view.ViewConfiguration
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.os.postAtTime
import com.chenfei.library.R

/**
 * 双击退出
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-04-13 13:36
 */
class DoubleBackToExitCallback(
    private val context: Context, private val handler: Handler
) : OnBackPressedCallback(true) {
    private val doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout()

    override fun handleOnBackPressed() {
        ToastUtil.showShort(context, R.string.main_doubleBackToExit)
        isEnabled = false
        handler.postAtTime(doubleTapTimeout * 7L) {
            isEnabled = true
        }
    }

    companion object {
        fun setup(context: ComponentActivity) {
            context.onBackPressedDispatcher.addCallback(
                context, DoubleBackToExitCallback(context, context.safeHandler)
            )
        }
    }
}
