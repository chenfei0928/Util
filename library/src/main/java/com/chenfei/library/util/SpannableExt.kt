package com.chenfei.library.util

import android.content.Context
import android.os.Build
import android.support.annotation.DrawableRes
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan

fun Editable.appendDrawable(context: Context, @DrawableRes drawableRes: Int) {
    appendDrawable(context, drawableRes, DynamicDrawableSpan.ALIGN_BASELINE)
}

/**
 * [android.text.Html.startImg(Editable, org.xml.sax.Attributes, android.text.Html.ImageGetter)]
 */
fun Editable.appendDrawable(context: Context, @DrawableRes drawableRes: Int, verticalAlignment: Int) {
    appendSpan("\uFFFC", ImageSpan(context, drawableRes, verticalAlignment))
}

fun Editable.appendSpan(text: CharSequence, vararg what: Any) {
    appendSpan(text, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE, *what)
}

fun Editable.appendSpan(text: CharSequence, flag: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE, vararg what: Any) {
    if (what.isEmpty()) {
        append(text)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && what.size == 1
            && this is SpannableStringBuilder) {
        append(text, what[0], flag)
    } else {
        val start = length
        append(text)
        val end = length
        for (o in what) {
            setSpan(o, start, end, flag)
        }
    }
}