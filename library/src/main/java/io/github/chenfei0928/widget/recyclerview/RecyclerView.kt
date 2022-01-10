package io.github.chenfei0928.widget.recyclerview

import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.*
import com.google.android.flexbox.FlexboxLayoutManager
import io.github.chenfei0928.view.ViewTagDelegate
import io.github.chenfei0928.view.getTagOrPut
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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
            return LinearSmoothScroller.SNAP_TO_START
        }
    }
    smoothScroller.targetPosition = position
    layoutManager?.startSmoothScroll(smoothScroller)
}

/**
 * 接近目标猎物
 * 在开始滑动[RecyclerView.smoothMoveToPosition]前尽可能的接近目标，以减少其后续滑动时间
 *
 * 在此方法中会计算出列表可视区域范围，之后根据给出的滚动目标位置将列表滑动到其附近
 *
 * @param position [RecyclerView.smoothMoveToPosition]的目标下标
 * @param maxOffsetSize 在进行滑动动画前让列表在其范围外最大多少个item位置停靠
 */
fun RecyclerView.nearToPosition(position: Int, maxOffsetSize: Int = 5) {
    // 获取其item可见区域范围
    val visibleRange = when (val lm = layoutManager) {
        null -> {
            return
        }
        is LinearLayoutManager -> {
            lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()
        }
        is GridLayoutManager -> {
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
        is FlexboxLayoutManager -> {
            lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()
        }
        else -> try {
            lm.javaClass.run {
                val firstVisibleItem = getMethod("findFirstVisibleItemPosition").invoke(lm) as Int
                val lastVisibleItem = getMethod("findLastVisibleItemPosition").invoke(lm) as Int
                firstVisibleItem..lastVisibleItem
            }
        } catch (e: Exception) {
            0..Int.MAX_VALUE
        }
    }
    // 如果目标在可见范围外，先计算一个期望的列表滚动动画开始时的下标，防止动画滚动时间太长
    when {
        position < visibleRange.first -> {
            min(position + maxOffsetSize, visibleRange.first)
        }
        position > visibleRange.last -> {
            max(position - maxOffsetSize, visibleRange.last)
        }
        else -> {
            visibleRange.first
        }
    }.let { scrollAnimationStartPosition ->
        // 如果计算出的动画开始时的下标在可见范围外，将列表滑动到滚动开始时的位置
        if (scrollAnimationStartPosition !in visibleRange) {
            scrollToPosition(scrollAnimationStartPosition)
        }
    }
}

fun RecyclerView.Adapter<*>?.isNullOrEmpty(): Boolean {
    return this == null || isEmpty()
}

fun RecyclerView.Adapter<*>.isEmpty(): Boolean {
    return itemCount == 0
}

/**
 * 字段委托类，通过viewTag来实现为viewHolder扩展字段
 */
class ViewHolderTagDelegate<R>(
    @IdRes id: Int
) : ReadWriteProperty<RecyclerView.ViewHolder, R?> {
    private var View.delegateImpl: R? by ViewTagDelegate(id)

    override fun getValue(thisRef: RecyclerView.ViewHolder, property: KProperty<*>): R? {
        return thisRef.itemView.delegateImpl
    }

    override fun setValue(thisRef: RecyclerView.ViewHolder, property: KProperty<*>, value: R?) {
        thisRef.itemView.delegateImpl = value
    }
}

/**
 * 字段委托类，通过viewTag来实现为viewHolder扩展字段
 */
class ViewHolderTagValDelegate<VH : RecyclerView.ViewHolder, R>(
    @IdRes private val id: Int, private val creator: (VH) -> R
) : ReadOnlyProperty<VH, R> {

    override fun getValue(thisRef: VH, property: KProperty<*>): R {
        return thisRef.itemView.getTagOrPut(id) {
            creator(thisRef)
        }
    }
}
