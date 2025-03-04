package io.github.chenfei0928.base.fragment.lazy

import android.view.Choreographer
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import io.github.chenfei0928.app.fragment.findFragment

/**
 * @author chenf()
 * @date 2025-03-03 18:33
 */
class FragmentViewPagerPreloadListener(
    private val fragmentManager: FragmentManager,
    private val viewPager2: ViewPager2,
) : ViewPager2.OnPageChangeCallback(), Choreographer.FrameCallback {

    override fun onPageSelected(position: Int) {
        Choreographer.getInstance().removeFrameCallback(this)
        Choreographer.getInstance().postFrameCallbackDelayed(this, 100)
    }

    override fun doFrame(frameTimeNanos: Long) {
        val position = viewPager2.currentItem
        val previous = viewPager2.findFragment(fragmentManager, position - 1)
        if (previous is LazyInitFragment) {
            previous.checkInflate()
        }
        val next = viewPager2.findFragment(fragmentManager, position + 1)
        if (next is LazyInitFragment) {
            next.checkInflate()
        }
    }
}
