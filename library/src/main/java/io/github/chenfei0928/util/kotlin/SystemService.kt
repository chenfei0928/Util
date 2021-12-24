package io.github.chenfei0928.util.kotlin

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-08 18:11
 */
fun View.hideSoftInputFromWindow() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}
