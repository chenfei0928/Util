package io.github.chenfei0928.viewModel

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import io.github.chenfei0928.widget.TextWatcherAdapter

/**
 * 使View监听其他的EditText内容不为空时才可用
 */
fun View.watchEditorNotEmpty(vararg editors: EditText): TextWatcher {
    return watchEditorNotEmpty(*editors) {
        isEnabled = it
    }
}

inline fun View.watchEditorNotEmpty(
    vararg editors: EditText, crossinline action: View.(Boolean) -> Unit
): TextWatcher {
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
