package com.chenfei.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import android.widget.AbsListView
import android.widget.GridView
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.viewpager.widget.ViewPager
import com.chenfei.util.Log

/**
 * [博文](https://droidyue.com/blog/2019/01/27/webview-javascript-scrolling-issue/)
 * [Github来源](https://github.com/androidyue/WebViewViewPagerScrollingIssue/blob/master/app/src/main/java/com/example/secoo/webviewandviewpagerscrollsample/MyWebView.kt)
 */
open class NestedScrollingWebView
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedWebView(context, attrs, defStyleAttr) {

    private fun dumpMessage(message: String) {
        Log.i(TAG, "dumpMessage message=$message")
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val viewParent = findViewParentIfNeeds(this)
            viewParent?.requestDisallowInterceptTouchEvent(true)
        }
        return super.onTouchEvent(event)
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        dumpMessage(
            "onOverScrolled scrollX=" + scrollX + ";scrollY=" + scrollY + ";clampedX=" + clampedX + ";clampedY=" + clampedY
        )
        if (clampedX) {
            val viewParent = findViewParentIfNeeds(this)
            viewParent?.requestDisallowInterceptTouchEvent(false)
        }
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
    }

    internal fun findViewParentIfNeeds(tag: View): ViewParent? {
        val parent = tag.parent
        if (parent == null) {
            return parent
        }
        return if (parent is ViewPager || parent is AbsListView || parent is ScrollView || parent is HorizontalScrollView || parent is GridView) {
            parent
        } else {
            if (parent is View) {
                findViewParentIfNeeds(parent as View)
            } else {
                parent
            }
        }
    }

    companion object {
        private const val TAG = "NestedScrollingWebView"
    }
}