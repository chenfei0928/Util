package io.github.chenfei0928.widget

import android.text.Editable
import android.text.TextWatcher

/**
 * Created by MrFeng on 2018/5/14.
 */
open class TextWatcherAdapter : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable) {
    }
}
