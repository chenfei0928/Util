package io.github.chenfei0928.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import org.jetbrains.anko.*

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

    fun getString(@StringRes id: Int, vararg args: Any?): String = context.getString(id, *args)

    @ColorInt
    fun getColor(@ColorRes id: Int): Int = ContextCompat.getColor(context, id)
    fun getColorStateList(@ColorRes id: Int): ColorStateList? =
        ContextCompat.getColorStateList(context, id)

    fun getDrawable(@DrawableRes id: Int): Drawable? = ContextCompat.getDrawable(context, id)

    inline fun dipI(value: Float): Int = context.dip(value)
    inline fun dipF(value: Float): Float = context.dipF(value)
    inline fun spI(value: Float): Int = context.sp(value)
    inline fun spF(value: Float): Float = context.spF(value)
    inline fun dimen(@DimenRes resource: Int): Int = context.dimen(resource)
    inline fun dimenF(@DimenRes resource: Int): Float = context.dimenF(resource)
}

open class ViewBindingHolder<T, V : ViewBinding>(
    val viewBinding: V
) : ViewHolder<T>(viewBinding.root)
