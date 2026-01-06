package io.github.chenfei0928.widget.recyclerview

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import io.github.chenfei0928.util.DependencyChecker
import io.github.chenfei0928.util.Log
import kotlin.math.max
import kotlin.math.min

private const val TAG = "Ut_RecyclerView"

fun RecyclerView.smoothScrollToTop() {
    smoothMoveToPosition(0)
}

/**
 * [相关来源](https://www.jianshu.com/p/b971ca7729c1)
 */
fun RecyclerView.smoothMoveToPosition(position: Int) {
    nearToPosition(position)
    val smoothScroller = object : LinearSmoothScroller(context) {
        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }
    }
    smoothScroller.targetPosition = position
    layoutManager?.startSmoothScroll(smoothScroller)
}

/**
 * 查询列表可见区域范围
 */
@Suppress("NestedBlockDepth")
fun RecyclerView.findVisibleRange(): IntRange = when (val lm = layoutManager) {
    null -> {
        throw IllegalArgumentException("no layoutManger")
    }
    is GridLayoutManager -> {
        lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()
    }
    is LinearLayoutManager -> {
        lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()
    }
    is StaggeredGridLayoutManager -> {
        // 获取列表各列第一项
        val positionsCache = IntArray(lm.spanCount) { 0 }
        lm.findFirstVisibleItemPositions(positionsCache)
        val firstVisibleItem = positionsCache.minOrNull() ?: 0
        // 获取列表各列最后一项
        positionsCache.fill(Int.MAX_VALUE)
        lm.findLastVisibleItemPositions(positionsCache)
        val lastVisibleItem = positionsCache.maxOrNull() ?: Int.MAX_VALUE
        // 列表显示范围
        firstVisibleItem..lastVisibleItem
    }
    else -> if (DependencyChecker.flexBox && lm is FlexboxLayoutManager) {
        lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()
    } else try {
        lm.javaClass.run {
            val firstVisibleItem = getMethod("findFirstVisibleItemPosition").invoke(lm) as Int
            val lastVisibleItem = getMethod("findLastVisibleItemPosition").invoke(lm) as Int
            firstVisibleItem..lastVisibleItem
        }
    } catch (e: ReflectiveOperationException) {
        Log.d(TAG, "nearToPosition: cannot find visible range ${lm.javaClass.name} $lm", e)
        0..Int.MAX_VALUE
    }
}

/**
 * 接近目标猎物
 * 在开始滑动[RecyclerView.smoothMoveToPosition]前尽可能的接近目标，以减少其后续滑动时间
 *
 * 在此方法中会计算出列表可视区域范围，之后根据给出的滚动目标位置将列表滑动到其附近
 *
 * @param targetPosition [RecyclerView.smoothMoveToPosition]的目标下标
 * @param maxOffsetSize 在进行滑动动画前让列表在其范围外最大多少个item位置停靠
 * @param visibleRange 其item可见区域范围
 */
fun RecyclerView.nearToPosition(
    targetPosition: Int,
    maxOffsetSize: Int = 5,
    visibleRange: IntRange = findVisibleRange()
) {
    // 如果目标在可见范围外，先计算一个期望的列表滚动动画开始时的下标，防止动画滚动时间太长
    val scrollAnimationStartPosition = when {
        targetPosition < visibleRange.first -> {
            // 目标在当前列表展示范围之上
            // 实际测试时由上方向下方滑动时，该项目会在顶部。
            // 预滑动到目标下方的一定位置
            min(targetPosition + maxOffsetSize, visibleRange.first)
        }
        targetPosition > visibleRange.last -> {
            // 目标在当前列表展示范围之下
            // 实际测试时由下方向上方滑动时，该项目会在低部。
            // 预滑动到目标上方一定位置
            max(targetPosition - maxOffsetSize, visibleRange.last)
        }
        else -> {
            // 目标在可见范围内，不处理直接返回
            return
        }
    }
    // 如果计算出的动画开始时的下标在可见范围外，将列表滑动到滚动开始时的位置
    if (scrollAnimationStartPosition !in visibleRange) {
        // 文档说明为只负责滑动到指定位置，但该项目具体位置由LayoutManager实现
        scrollToPosition(scrollAnimationStartPosition)
    }
}

fun RecyclerView.Adapter<*>?.isNullOrEmpty(): Boolean {
    return this == null || isEmpty()
}

fun RecyclerView.Adapter<*>.isEmpty(): Boolean {
    return itemCount == 0
}
