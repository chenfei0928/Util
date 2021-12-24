package io.github.chenfei0928.adapter.zhy

import androidx.recyclerview.widget.RecyclerView

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-10-22 16:47
 */
class WrapperAdapterDataObserver(
        private val adapter: WrapperAdapter
) : RecyclerView.AdapterDataObserver() {

    override fun onChanged() {
        adapter.notifyDataSetChanged()
    }

    private fun checkAdapterEmpty(): Boolean {
        return if (adapter.lastIsEmpty) {
            adapter.notifyDataSetChanged()
            true
        } else {
            false
        }
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        if (checkAdapterEmpty()) {
            return
        }
        adapter.notifyItemRangeRemoved(adapter.headerSize + positionStart, itemCount)
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        if (checkAdapterEmpty()) {
            return
        }
        if (itemCount == 1) {
            adapter.notifyItemMoved(adapter.headerSize + fromPosition, adapter.headerSize + toPosition)
        } else {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        if (checkAdapterEmpty()) {
            return
        }
        adapter.notifyItemRangeInserted(adapter.headerSize + positionStart, itemCount)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        if (checkAdapterEmpty()) {
            return
        }
        adapter.notifyItemRangeChanged(adapter.headerSize + positionStart, itemCount)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        if (checkAdapterEmpty()) {
            return
        }
        adapter.notifyItemRangeChanged(adapter.headerSize + positionStart, itemCount, payload)
    }
}
