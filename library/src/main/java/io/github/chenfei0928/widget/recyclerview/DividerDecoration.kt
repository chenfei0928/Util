package io.github.chenfei0928.widget.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.res.use
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.min

/**
 * 用于给RecyclerView添加间隔行的工具类，取自库：
 * com.timehop.stickyheadersrecyclerview:library
 * Created by Admin on 2016/1/27.
 */
class DividerDecoration(
    private val mDivider: Drawable
) : RecyclerView.ItemDecoration() {

    constructor(context: Context) : this(
        context.obtainStyledAttributes(ATTRS).use { it.getDrawable(0)!! }
    )

    private fun getOrientation(parent: RecyclerView): Int {
        try {
            return (parent.layoutManager as LinearLayoutManager).orientation
        } catch (e: ClassCastException) {
            throw IllegalStateException(
                "DividerDecoration can only be used with a LinearLayoutManager.", e
            )
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val recyclerViewTop = parent.paddingTop
        val recyclerViewBottom = parent.height - parent.paddingBottom
        val intrinsicHeight = mDivider.intrinsicHeight
        for (child in parent.children) {
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = max(recyclerViewTop, child.bottom + params.bottomMargin)
            val bottom = min(recyclerViewBottom, top + intrinsicHeight)
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom
        val recyclerViewLeft = parent.paddingLeft
        val recyclerViewRight = parent.width - parent.paddingRight
        val intrinsicHeight = mDivider.intrinsicHeight
        for (child in parent.children) {
            val params = child.layoutParams as RecyclerView.LayoutParams
            val left = max(recyclerViewLeft, child.right + params.rightMargin)
            val right = min(recyclerViewRight, left + intrinsicHeight)
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        @Suppress("kotlin:S6518")
        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, 0, mDivider.intrinsicHeight)
        } else {
            outRect.set(0, 0, mDivider.intrinsicWidth, 0)
        }
    }

    companion object {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }
}
