package org.jetbrains.anko

import android.text.Editable
import android.text.Spanned

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-05-16 16:26
 */

fun Editable.append(text: CharSequence, span: Any) {
    val textLength = text.length
    append(text)
    setSpan(span, this.length - textLength, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
}

fun Editable.append(text: CharSequence, vararg spans: Any) {
    val textLength = text.length
    append(text)
    spans.forEach { span ->
        setSpan(span, this.length - textLength, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}
