package io.github.chenfei0928.widget.recyclerview.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dimenF
import org.jetbrains.anko.dip
import org.jetbrains.anko.dipF
import org.jetbrains.anko.sp
import org.jetbrains.anko.spF

/**
 * User: ChenFei(chenfei0928@gmail.com)
 * Date: 2019-03-21
 * Time: 17:07
 */
@Suppress("NOTHING_TO_INLINE")
open class ViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView),
    RecyclerViewHolderModelProvider<T> {

    override var item: T? = null
    open val context: Context
        get() = itemView.context

    inline fun getString(@StringRes id: Int, vararg args: Any?): String =
        context.getString(id, *args)

    @ColorInt
    inline fun getColor(@ColorRes id: Int): Int =
        ContextCompat.getColor(context, id)

    inline fun getColorStateList(@ColorRes id: Int): ColorStateList? =
        ContextCompat.getColorStateList(context, id)

    inline fun getDrawable(@DrawableRes id: Int): Drawable? =
        ContextCompat.getDrawable(context, id)

    @Px
    inline fun dipI(@Dimension(Dimension.DP) value: Float): Int = context.dip(value)

    @Px
    inline fun dipF(@Dimension(Dimension.DP) value: Float): Float = context.dipF(value)

    @Px
    inline fun spI(@Dimension(Dimension.SP) value: Float): Int = context.sp(value)

    @Px
    inline fun spF(@Dimension(Dimension.SP) value: Float): Float = context.spF(value)

    @Px
    inline fun dimen(@DimenRes resource: Int): Int = context.dimen(resource)

    @Px
    inline fun dimenF(@DimenRes resource: Int): Float = context.dimenF(resource)
}
