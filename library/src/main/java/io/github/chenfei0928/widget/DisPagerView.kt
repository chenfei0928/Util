package io.github.chenfei0928.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * 禁用了左右滑动的ViewPager，用来管理Fragment最简单实现
 * Created by Admin on 2015/11/23.
 */
open class DisPagerView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewPager(context, attrs) {
    var allowScroll = false

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return allowScroll && super.onInterceptTouchEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return allowScroll && super.onTouchEvent(ev)
    }
}
