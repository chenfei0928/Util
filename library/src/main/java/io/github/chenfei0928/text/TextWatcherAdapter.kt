package io.github.chenfei0928.text

import android.text.Editable
import android.text.TextWatcher
import androidx.annotation.EmptySuper

/**
 * Created by MrFeng on 2018/5/14.
 */
open class TextWatcherAdapter : TextWatcher {
    @EmptySuper
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // noop
    }

    @EmptySuper
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        // noop
    }

    @EmptySuper
    override fun afterTextChanged(s: Editable) {
        // noop
    }
}
