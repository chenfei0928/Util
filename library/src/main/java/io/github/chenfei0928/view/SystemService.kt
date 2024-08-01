package io.github.chenfei0928.view

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-08 18:11
 */
fun View.hideSoftInputFromWindow() {
    context.getSystemService<InputMethodManager>()
        ?.hideSoftInputFromWindow(this.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}
