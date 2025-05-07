@file:JvmName("DimensionsCf0928Util")

package org.jetbrains.anko

import android.content.Context
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.core.util.TypedValueCompat
import androidx.fragment.app.Fragment

//returns dip(dp) dimension value in pixels
@Px
inline fun Context.dip(@Dimension(Dimension.DP) value: Float): Int =
    (dipF(value) + (if (value > 0) 0.5f else -0.5f)).toInt()

@Px
inline fun Context.dipF(@Dimension(Dimension.DP) value: Float): Float =
    TypedValueCompat.dpToPx(value, resources.displayMetrics)

//return sp dimension value in pixels
@Px
inline fun Context.sp(@Dimension(Dimension.SP) value: Float): Int =
    (spF(value) + (if (value > 0) 0.5f else -0.5f)).toInt()

@Px
inline fun Context.spF(@Dimension(Dimension.SP) value: Float): Float =
    TypedValueCompat.spToPx(value, resources.displayMetrics)

//converts px value into dip or sp
@Dimension(Dimension.DP)
inline fun Context.px2dip(@Px px: Int): Float =
    TypedValueCompat.pxToDp(px.toFloat(), resources.displayMetrics)

@Dimension(Dimension.SP)
inline fun Context.px2sp(@Px px: Int): Float =
    TypedValueCompat.pxToSp(px.toFloat(), resources.displayMetrics)

@Px
inline fun Context.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)

@Px
inline fun Context.dimenF(@DimenRes resource: Int): Float = resources.getDimension(resource)

//the same for the views
@Px
inline fun View.dip(@Dimension(Dimension.DP) value: Float): Int = context.dip(value)

@Px
inline fun View.dipF(@Dimension(Dimension.DP) value: Float): Float = context.dipF(value)

@Px
inline fun View.sp(@Dimension(Dimension.SP) value: Float): Int = context.sp(value)

@Dimension(Dimension.DP)
inline fun View.px2dip(@Px px: Int): Float = context.px2dip(px)

@Dimension(Dimension.SP)
inline fun View.px2sp(@Px px: Int): Float = context.px2sp(px)

@Px
inline fun View.dimen(@DimenRes resource: Int): Int = context.dimen(resource)

//the same for the Fragment
@Px
inline fun Fragment.dip(@Dimension(Dimension.DP) value: Float): Int = requireActivity().dip(value)

@Px
inline fun Fragment.dipF(@Dimension(Dimension.DP) value: Float): Float =
    requireActivity().dipF(value)

@Px
inline fun Fragment.sp(@Dimension(Dimension.SP) value: Float): Int = requireActivity().sp(value)

@Px
inline fun Fragment.spF(@Dimension(Dimension.SP) value: Float): Float = requireActivity().spF(value)

@Dimension(Dimension.DP)
inline fun Fragment.px2dip(@Px px: Int): Float = requireActivity().px2dip(px)

@Dimension(Dimension.SP)
inline fun Fragment.px2sp(@Px px: Int): Float = requireActivity().px2sp(px)

@Px
inline fun Fragment.dimen(@DimenRes resource: Int): Int = requireActivity().dimen(resource)
