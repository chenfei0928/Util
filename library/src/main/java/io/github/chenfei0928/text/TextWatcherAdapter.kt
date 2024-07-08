package io.github.chenfei0928.text

import android.text.Editable
import android.text.TextWatcher

/**
 * Created by MrFeng on 2018/5/14.
 */
open class TextWatcherAdapter : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // noop
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        // noop
    }

    override fun afterTextChanged(s: Editable) {
        // noop
    }
}
