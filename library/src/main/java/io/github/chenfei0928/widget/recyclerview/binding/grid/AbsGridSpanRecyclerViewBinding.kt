package io.github.chenfei0928.widget.recyclerview.binding.grid

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.chenfei0928.collection.RecyclerViewAdapterDataObserverMultiList
import io.github.chenfei0928.util.Log
import io.github.chenfei0928.widget.recyclerview.binding.AbsLayoutParamRecyclerViewBinding

/**
 * 提供可以用span处理的[RecyclerView]列表填充框架（多span[GridLayoutManager]）。
 * 用于优化允许存在多列子列表的[RecyclerView]，并管理子item边距、spanSize。
 *
 * 对[AbsLayoutParamRecyclerViewBinding]进行了添加[GridLayoutManager]span特性支持，
 * 并对其使用场景中常遇到的并排项目间距，与其距离容器边距不一致的处理支持
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-17 19:07
 */
abstract class AbsGridSpanRecyclerViewBinding<LP : AbsGridSpanRecyclerViewBinding.LayoutParams>(
    contentView: RecyclerView,
    protected val spanCount: Int,
    list: MutableList<Any> = RecyclerViewAdapterDataObserverMultiList()
) : AbsLayoutParamRecyclerViewBinding<LP>(contentView, list) {

    init {
        contentView.layoutManager = GridLayoutManager(contentView.context, spanCount).apply {
            // layoutManager，并为其设置span
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    // 如果在header或footer内，span占满
                    if (position < adapterWrapperHeaderSize || position >= adapterWrapperHeaderSize + list.size) {
                        return spanCount
                    }
                    // 根据其在RecyclerView中的位置，找到其显示的bean实例
                    val any = list[position - adapterWrapperHeaderSize]
                    // 获取其布局参数信息或使用默认span值
                    return layoutParamsRecord[any]?.spanSize ?: run {
                        Log.d(TAG, "getSpanSize: $position $any layoutParams not found")
                        1
                    }
                }
            }
        }
        // 边距提供
        contentView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                // 根据其视图获取布局参数，以获取其边距信息
                outRect.set(getLayoutParams(view)?.marginInfo ?: NONE)
            }
        })
    }

    /**
     * 生成布局参数，如果之类扩展了自己的布局参数[LP]，也要提供一个用于生成子类布局参数的接口
     */
    override fun generateDefaultLayoutParams(): LP {
        @Suppress("UNCHECKED_CAST")
        return LayoutParams(spanCount, NONE) as LP
    }

    companion object {
        private const val TAG = "KW_AbsSpanRecyclerViewB"

        @JvmStatic
        val NONE = Rect()
    }

    open class LayoutParams(
        val spanSize: Int, val marginInfo: Rect
    ) : AbsLayoutParamRecyclerViewBinding.LayoutParams()
}
