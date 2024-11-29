package io.github.chenfei0928.text

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import androidx.annotation.DrawableRes
import org.jetbrains.anko.append
import java.text.FieldPosition
import java.text.Format

/**
 * [android.text.Html.startImg(Editable, org.xml.sax.Attributes, android.text.Html.ImageGetter)]
 */
@JvmOverloads
fun SpannableStringBuilder.appendDrawable(
    context: Context,
    @DrawableRes drawableRes: Int,
    verticalAlignment: Int = DynamicDrawableSpan.ALIGN_BASELINE
) = if (drawableRes == Resources.ID_NULL) {
    this
} else append("\uFFFC", ImageSpan(context, drawableRes, verticalAlignment))

/**
 * [android.text.Html.startImg(Editable, org.xml.sax.Attributes, android.text.Html.ImageGetter)]
 */
fun SpannableStringBuilder.appendDrawable(
    drawable: Drawable, verticalAlignment: Int = DynamicDrawableSpan.ALIGN_BASELINE
) = append("\uFFFC", ImageSpan(drawable, verticalAlignment))

/**
 * [java.text.DontCareFieldPosition]
 */
private val fieldPosition = FieldPosition(0)

fun StringBuffer.appendFormat(format: Format, data: Any) =
    format.format(data, this, fieldPosition)
