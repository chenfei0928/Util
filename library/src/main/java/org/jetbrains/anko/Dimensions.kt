package org.jetbrains.anko

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.core.util.TypedValueCompat
import androidx.fragment.app.Fragment

//returns dip(dp) dimension value in pixels
inline fun Context.dip(@Dimension(Dimension.DP) value: Float): Int = dipF(value).toInt()
inline fun Context.dipF(
    @Dimension(Dimension.DP) value: Float
): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics
)

//return sp dimension value in pixels
inline fun Context.sp(@Dimension(Dimension.SP) value: Float): Int = spF(value).toInt()
inline fun Context.spF(
    @Dimension(Dimension.SP) value: Float
): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics
)

//converts px value into dip or sp
inline fun Context.px2dip(@Px px: Int): Float =
    TypedValueCompat.pxToDp(px.toFloat(), resources.displayMetrics)

inline fun Context.px2sp(@Px px: Int): Float =
    TypedValueCompat.pxToSp(px.toFloat(), resources.displayMetrics)

inline fun Context.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)
inline fun Context.dimenF(@DimenRes resource: Int): Float = resources.getDimension(resource)

//the same for the views
inline fun View.dip(@Dimension(Dimension.DP) value: Float): Int = context.dip(value)
inline fun View.dipF(@Dimension(Dimension.DP) value: Float): Float = context.dipF(value)
inline fun View.sp(@Dimension(Dimension.SP) value: Float): Int = context.sp(value)
inline fun View.px2dip(@Px px: Int): Float = context.px2dip(px)
inline fun View.px2sp(@Px px: Int): Float = context.px2sp(px)
inline fun View.dimen(@DimenRes resource: Int): Int = context.dimen(resource)

inline fun Fragment.dip(@Dimension(Dimension.DP) value: Float): Int = requireActivity().dip(value)
inline fun Fragment.dipF(@Dimension(Dimension.DP) value: Float): Float =
    requireActivity().dipF(value)

inline fun Fragment.sp(@Dimension(Dimension.SP) value: Float): Int = requireActivity().sp(value)
inline fun Fragment.spF(@Dimension(Dimension.SP) value: Float): Float = requireActivity().spF(value)
inline fun Fragment.px2dip(@Px px: Int): Float = requireActivity().px2dip(px)
inline fun Fragment.px2sp(@Px px: Int): Float = requireActivity().px2sp(px)
inline fun Fragment.dimen(@DimenRes resource: Int): Int = requireActivity().dimen(resource)
