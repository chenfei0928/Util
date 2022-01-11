/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-11 11:25
 */
package io.github.chenfei0928.widget

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.TintAwareDrawable
import androidx.core.widget.TintableImageSourceView

fun ImageView.setSrcTintRes(@ColorRes colorRes: Int) {
    val colorStateList = ContextCompat.getColorStateList(this.context, colorRes)
    setSrcTint(colorStateList)
}

fun ImageView.setSrcTint(colorStateList: ColorStateList?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.imageTintList = colorStateList
    } else if (this is TintableImageSourceView) {
        val tintView = this as TintableImageSourceView
        tintView.supportImageTintList = colorStateList
    } else {
        var drawable: Drawable = this.drawable ?: return
        if (drawable !is TintAwareDrawable) {
            drawable = DrawableCompat.wrap(drawable)
            this.setImageDrawable(drawable)
        }
        DrawableCompat.setTintList(drawable, colorStateList)
    }
}
