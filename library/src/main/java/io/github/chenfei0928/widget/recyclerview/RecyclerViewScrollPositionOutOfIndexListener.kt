package io.github.chenfei0928.widget.recyclerview

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 监听RecyclerView滑动，滑动到超过或回到[focusIndex]时触发回调
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-04-27 17:01
 */
abstract class RecyclerViewScrollPositionOutOfIndexListener : RecyclerView.OnScrollListener() {
    abstract val focusIndex: Int

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        // 下标不可用，不处理
        if (focusIndex < 0) {
            return
        }
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager

        // 处理菜单的显示隐藏
        if (dy == 0) {
            // 布局刚刚完成，初始化菜单ui
            onPositionOutOfFocusIndex(layoutManager.findFirstCompletelyVisibleItemPosition() > focusIndex + 1)
        } else if (dy < 0 && layoutManager.findFirstVisibleItemPosition() <= focusIndex) {
            onPositionOutOfFocusIndex(outOfIndex = false)
        } else if (dy > 0 && layoutManager.findFirstCompletelyVisibleItemPosition() > focusIndex + 1) {
            onPositionOutOfFocusIndex(outOfIndex = true)
        }
    }

    abstract fun onPositionOutOfFocusIndex(outOfIndex: Boolean)
}
