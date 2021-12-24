package io.github.chenfei0928.adapter

import androidx.recyclerview.widget.RecyclerView

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-10-22 16:47
 */
class DelegateAdapterDataObserver(
    private val adapter: RecyclerView.Adapter<*>
) : RecyclerView.AdapterDataObserver() {

    override fun onChanged() {
        adapter.notifyDataSetChanged()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        adapter.notifyItemRangeRemoved(positionStart, itemCount)
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        adapter.notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        adapter.notifyItemRangeInserted(positionStart, itemCount)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        adapter.notifyItemRangeChanged(positionStart, itemCount)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        adapter.notifyItemRangeChanged(positionStart, itemCount, payload)
    }
}
