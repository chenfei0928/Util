package com.chenfei.adapter.zhy

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * [Zhy的BaseAdapter工具类](https://github.com/hongyangAndroid/baseAdapter/blob/master/baseadapter-recyclerview/src/main/java/com/zhy/adapter/recyclerview/utils/WrapperUtils.java)
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-10-22 18:45
 */
class WrapperUtils {
    companion object {

        fun onAttachedToRecyclerView(
            innerAdapter: RecyclerView.Adapter<*>,
            recyclerView: RecyclerView,
            callback: (layoutManager: GridLayoutManager, oldLookup: GridLayoutManager.SpanSizeLookup, position: Int) -> Int
        ) {
            innerAdapter.onAttachedToRecyclerView(recyclerView)

            val layoutManager = recyclerView.layoutManager
            if (layoutManager is GridLayoutManager) {
                val spanSizeLookup = layoutManager.spanSizeLookup

                layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return callback(layoutManager, spanSizeLookup, position)
                    }
                }
                layoutManager.spanCount = layoutManager.spanCount
            }
        }

        fun setFullSpan(holder: RecyclerView.ViewHolder) {
            val lp = holder.itemView.layoutParams
            if (lp is StaggeredGridLayoutManager.LayoutParams) {
                lp.isFullSpan = true
            }
        }
    }
}
