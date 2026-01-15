@file:JvmName("BuildSpannedCf0928Util")

package org.jetbrains.anko

import android.text.Editable
import android.text.Spanned
import androidx.annotation.ReturnThis

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-05-16 16:26
 */
@ReturnThis
fun <T : Editable> T.append(text: CharSequence, span: Any): T {
    val textLength = text.length
    append(text)
    setSpan(span, this.length - textLength, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    return this
}

@ReturnThis
fun <T : Editable> T.append(text: CharSequence, vararg spans: Any): T {
    val textLength = text.length
    append(text)
    spans.forEach { span ->
        setSpan(span, this.length - textLength, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
    return this
}
