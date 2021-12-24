package io.github.chenfei0928.adapter

import androidx.recyclerview.widget.RecyclerView

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-10-29 15:36
 */
class NotifyDataSetChangedDataObserver(
    private val adapter: RecyclerView.Adapter<*>
) : RecyclerView.AdapterDataObserver() {

    override fun onChanged() {
        adapter.notifyDataSetChanged()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        adapter.notifyDataSetChanged()
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        adapter.notifyDataSetChanged()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        adapter.notifyDataSetChanged()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        adapter.notifyDataSetChanged()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        adapter.notifyDataSetChanged()
    }
}
