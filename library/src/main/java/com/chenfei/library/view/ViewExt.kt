package com.chenfei.library.view

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText

/**
 * 使View监听其他的EditText内容不为空时才可用
 */
fun View.watchEditorNotEmpty(vararg editors: EditText) {
    fun checkEditorNotEmpty() {
        this.isEnabled = editors.find { it.length() <= 0 } == null
    }
    checkEditorNotEmpty()
    val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            checkEditorNotEmpty()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }
    editors.forEach {
        it.addTextChangedListener(textWatcher)
    }
}
