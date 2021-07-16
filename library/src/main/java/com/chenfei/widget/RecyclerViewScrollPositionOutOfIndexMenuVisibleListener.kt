package com.chenfei.widget

import android.animation.ValueAnimator
import android.view.MenuItem

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
    private var iconAnimator: ValueAnimator? = null

    override fun onPositionOutOfFocusIndex(outOfIndex: Boolean) {
        if (targetSearchBarVisible == outOfIndex) {
            return
        }
        val menu = menuGetter() ?: return
        val icon = menu.icon ?: return
        val animator = iconAnimator ?: ValueAnimator
            .ofInt(0, 255)
            .apply {
                duration = 300L
                addUpdateListener {
                    val alpha = it.animatedValue as Int
                    icon.alpha = alpha
                    icon.invalidateSelf()
                    menu.isEnabled = alpha != 0
                }
                iconAnimator = this
            }
        animator.setIntValues(
            *if (outOfIndex) {
                intArrayOf(0, 255)
            } else {
                intArrayOf(255, 0)
            }
        )
        animator.start()
        targetSearchBarVisible = outOfIndex
    }
}
