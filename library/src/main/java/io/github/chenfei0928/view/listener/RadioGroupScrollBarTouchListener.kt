package io.github.chenfei0928.view.listener

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach

/**
 * 侧栏滑动时选中其指示器
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-04-28 16:04
 */
class RadioGroupScrollBarTouchListener(
    private val viewGroup: ViewGroup
) : View.OnTouchListener {

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // 判断点击坐标是否在最右侧一列宽度中，如果是则为滑动指示器，消费整个滑动事件
            // 此处如返回false，后续事件将不会回调
            val firstChild = viewGroup.getChildAt(0)
            return event.x < firstChild.right && event.x > firstChild.left
        }
        if (event.action == MotionEvent.ACTION_MOVE) {
            // 处理滑动事件
            val y = event.y
            viewGroup.forEach { child ->
                // 判断滑动事件当前位置是否在该子view位置内
                // x轴坐标不检查，交由按下的事件处理
                if (child.top < y && y < child.bottom) {
                    // 选中该item
                    child.performClick()
                }
            }
            return true
        }
        return false
    }
}
