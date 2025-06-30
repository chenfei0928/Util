@file:JvmName("DimensionsCf0928Util")

@file:Suppress("TooManyFunctions")

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
fun Context.dip(@Dimension(Dimension.DP) value: Float): Int =
    (dipF(value) + (if (value > 0) 0.5f else -0.5f)).toInt()

@Px
fun Context.dipF(@Dimension(Dimension.DP) value: Float): Float =
    TypedValueCompat.dpToPx(value, resources.displayMetrics)

//return sp dimension value in pixels
@Px
fun Context.sp(@Dimension(Dimension.SP) value: Float): Int =
    (spF(value) + (if (value > 0) 0.5f else -0.5f)).toInt()

@Px
fun Context.spF(@Dimension(Dimension.SP) value: Float): Float =
    TypedValueCompat.spToPx(value, resources.displayMetrics)

//converts px value into dip or sp
@Dimension(Dimension.DP)
fun Context.px2dip(@Px px: Int): Float =
    TypedValueCompat.pxToDp(px.toFloat(), resources.displayMetrics)

@Dimension(Dimension.SP)
fun Context.px2sp(@Px px: Int): Float =
    TypedValueCompat.pxToSp(px.toFloat(), resources.displayMetrics)

@Px
fun Context.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)

@Px
fun Context.dimenF(@DimenRes resource: Int): Float = resources.getDimension(resource)

//the same for the views
@Px
fun View.dip(@Dimension(Dimension.DP) value: Float): Int = context.dip(value)

@Px
fun View.dipF(@Dimension(Dimension.DP) value: Float): Float = context.dipF(value)

@Px
fun View.sp(@Dimension(Dimension.SP) value: Float): Int = context.sp(value)

@Dimension(Dimension.DP)
fun View.px2dip(@Px px: Int): Float = context.px2dip(px)

@Dimension(Dimension.SP)
fun View.px2sp(@Px px: Int): Float = context.px2sp(px)

@Px
fun View.dimen(@DimenRes resource: Int): Int = context.dimen(resource)

//the same for the Fragment
@Px
fun Fragment.dip(@Dimension(Dimension.DP) value: Float): Int = requireActivity().dip(value)

@Px
fun Fragment.dipF(@Dimension(Dimension.DP) value: Float): Float =
    requireActivity().dipF(value)

@Px
fun Fragment.sp(@Dimension(Dimension.SP) value: Float): Int = requireActivity().sp(value)

@Px
fun Fragment.spF(@Dimension(Dimension.SP) value: Float): Float = requireActivity().spF(value)

@Dimension(Dimension.DP)
fun Fragment.px2dip(@Px px: Int): Float = requireActivity().px2dip(px)

@Dimension(Dimension.SP)
fun Fragment.px2sp(@Px px: Int): Float = requireActivity().px2sp(px)

@Px
fun Fragment.dimen(@DimenRes resource: Int): Int = requireActivity().dimen(resource)
