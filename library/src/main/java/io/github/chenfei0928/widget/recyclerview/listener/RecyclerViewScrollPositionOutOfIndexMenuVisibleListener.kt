package io.github.chenfei0928.widget.recyclerview.listener

import android.animation.ValueAnimator
import android.view.MenuItem
import io.github.chenfei0928.animation.ANIMATE_LAYOUT_CHANGES_DEFAULT_DURATION

/**
 * RecyclerView 滑动时隐藏、显示menu
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-05-27 15:15
 */
class RecyclerViewScrollPositionOutOfIndexMenuVisibleListener(
    private val menuGetter: () -> MenuItem?
) : RecyclerViewScrollPositionOutOfIndexListener() {
    override var focusIndex: Int = -1

    private var targetSearchBarVisible: Boolean = false
    private val iconAnimator: ValueAnimator by lazy {
        ValueAnimator.ofInt(0, 255).setDuration(ANIMATE_LAYOUT_CHANGES_DEFAULT_DURATION).apply {
            addUpdateListener {
                val menu = menuGetter()
                    ?: return@addUpdateListener
                val icon = menu.icon
                    ?: return@addUpdateListener
                val alpha = it.animatedValue as Int
                icon.alpha = alpha
                icon.invalidateSelf()
                menu.isEnabled = alpha != 0
            }
        }
    }

    override fun onPositionOutOfFocusIndex(outOfIndex: Boolean) {
        if (targetSearchBarVisible == outOfIndex) {
            return
        }
        if (outOfIndex) {
            iconAnimator.setIntValues(0, 255)
        } else {
            iconAnimator.setIntValues(255, 0)
        }
        iconAnimator.start()
        targetSearchBarVisible = outOfIndex
    }
}
