package io.github.chenfei0928.viewModel

import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.annotation.Px
import io.github.chenfei0928.widget.TextWatcherAdapter

/**
 * 使View监听其他的EditText内容不为空时才可用
 */
fun View.watchEditorNotEmpty(vararg editors: EditText): TextWatcher {
    return watchEditorNotEmpty(*editors) {
        isEnabled = it
    }
}

inline fun View.watchEditorNotEmpty(vararg editors: EditText, crossinline action: View.(Boolean) -> Unit): TextWatcher {
    val checkEditorNotEmpty = {
        action(this, editors.find { it.isEmpty() } == null)
    }
    checkEditorNotEmpty()
    val textWatcher = object : TextWatcherAdapter() {
        override fun afterTextChanged(s: Editable) {
            checkEditorNotEmpty()
        }
    }
    editors.forEach {
        it.addTextChangedListener(textWatcher)
    }
    return textWatcher
}

/**
 * 仅限在Java中使用
 */
fun View.setPaddingStart(@Px start: Int) {
    setPadding(start, paddingTop, paddingRight, paddingBottom)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        setPaddingRelative(start, paddingTop, paddingEnd, paddingBottom)
    }
}

/**
 * 仅限在Java中使用
 */
fun View.setPaddingTop(@Px top: Int) {
    setPadding(paddingLeft, top, paddingRight, paddingBottom)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        setPaddingRelative(paddingStart, top, paddingEnd, paddingBottom)
    }
}

/**
 * 仅限在Java中使用
 */
fun View.setPaddingEnd(@Px end: Int) {
    setPadding(paddingLeft, paddingTop, end, paddingBottom)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        setPaddingRelative(paddingStart, paddingTop, end, paddingBottom)
    }
}

/**
 * 仅限在Java中使用
 */
fun View.setPaddingBottom(@Px bottom: Int) {
    setPadding(paddingLeft, paddingTop, paddingRight, bottom)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        setPaddingRelative(paddingStart, paddingTop, paddingEnd, bottom)
    }
}

// Follows the same conversion mechanism as in TypedValue.complexToDimensionPixelSize as used
// when setting padding. It rounds off the float value unless the value is < 1.
// When a value is between 0 and 1, it is set to 1. A value less than 0 is set to -1.
fun Float.toPixelSize(): Int {
    val result = (this + 0.5f).toInt()
    return if (result != 0) {
        result
    } else if (this == 0f) {
        0
    } else if (this > 0) {
        1
    } else {
        -1
    }
}
