package io.github.chenfei0928.util

import android.content.Context
import android.os.Handler
import android.view.ViewConfiguration
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.os.postDelayed
import io.github.chenfei0928.util.R

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
        handler.postDelayed(doubleTapTimeout * 7L) {
            isEnabled = true
        }
    }

    companion object {
        fun setup(context: ComponentActivity) {
            context.onBackPressedDispatcher.addCallback(
                context, DoubleBackToExitCallback(context, SafeHandler.getOrCreate(context))
            )
        }
    }
}
