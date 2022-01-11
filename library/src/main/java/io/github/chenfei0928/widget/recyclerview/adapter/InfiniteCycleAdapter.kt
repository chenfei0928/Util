package io.github.chenfei0928.widget.recyclerview.adapter

import androidx.recyclerview.widget.RecyclerView

/**
 * 真实数据无限循环的适配器
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-10-29 15:15
 */
class InfiniteCycleAdapter<VH : RecyclerView.ViewHolder, Adapter : RecyclerView.Adapter<VH>>(
    innerAdapter: Adapter
) : AdapterDelegate<VH, Adapter>(innerAdapter) {

    override fun createObserver(): RecyclerView.AdapterDataObserver =
        NotifyDataSetChangedDataObserver(this)

    override fun getItemCount(): Int {
        return if (innerAdapter.itemCount == 0) {
            0
        } else {
            Int.MAX_VALUE
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        innerAdapter.onBindViewHolder(holder, position.toInnerPosition())
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        innerAdapter.onBindViewHolder(holder, position.toInnerPosition(), payloads)
    }

    override fun findRelativeAdapterPositionIn(
        adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
        viewHolder: RecyclerView.ViewHolder,
        localPosition: Int
    ): Int {
        return innerAdapter.findRelativeAdapterPositionIn(
            adapter, viewHolder, localPosition.toInnerPosition()
        )
    }

    override fun getItemViewType(position: Int): Int {
        return innerAdapter.getItemViewType(position.toInnerPosition())
    }

    override fun getItemId(position: Int): Long {
        return innerAdapter.getItemId(position.toInnerPosition())
    }

    private fun Int.toInnerPosition() = this % innerAdapter.itemCount
}
