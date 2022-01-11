package com.google.android.material.appbar

import androidx.annotation.CallSuper
import androidx.annotation.FloatRange
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.abs

/**
 * 根据AppBarLayout收起进度来调整ToolBar返回箭头的着色，类似[CollapsingToolbarLayout]Title效果
 * 部分逻辑参考 [CollapsingToolbarLayout.OffsetUpdateListener]
 *
 * @author MrFeng
 * @data 2017/7/19
 */
abstract class FractionOffsetChangedListener(
    private val collapsingToolbarLayout: CollapsingToolbarLayout
) : AppBarLayout.OnOffsetChangedListener {
    @FloatRange(from = 0.0, to = 1.0)
    var expandedFraction = 0f
        private set

    @CallSuper
    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val insetTop = WindowInsetsCompat.Type.statusBars()
        // Update the collapsing text's fraction
        val expandRange = collapsingToolbarLayout.height - ViewCompat.getMinimumHeight(
            collapsingToolbarLayout
        ) - insetTop
        expandedFraction = MathUtils.clamp(abs(verticalOffset) / expandRange.toFloat(), 0f, 1f)
        onExpandedFractionChanged(expandedFraction)
    }

    /**
     * @param expandedFraction 展开进度，0为已完全展开，1为已完全收起
     */
    abstract fun onExpandedFractionChanged(
        @FloatRange(
            from = 0.0, to = 1.0
        ) expandedFraction: Float
    )
}
