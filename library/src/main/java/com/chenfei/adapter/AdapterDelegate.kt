package com.chenfei.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-09-17 09:28
 */
open class AdapterDelegate<VH : RecyclerView.ViewHolder, Adapter : RecyclerView.Adapter<VH>>(
    protected val innerAdapter: Adapter
) : RecyclerView.Adapter<VH>() {

    init {
        innerAdapter.registerAdapterDataObserver(createObserver())
    }

    protected open fun createObserver(): RecyclerView.AdapterDataObserver =
        DelegateAdapterDataObserver(this)

    override fun getItemCount(): Int {
        return innerAdapter.itemCount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return innerAdapter.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        innerAdapter.onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        innerAdapter.onBindViewHolder(holder, position, payloads)
    }

    override fun findRelativeAdapterPositionIn(
        adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
        viewHolder: RecyclerView.ViewHolder,
        localPosition: Int
    ): Int {
        return innerAdapter.findRelativeAdapterPositionIn(innerAdapter, viewHolder, localPosition)
    }

    override fun getItemViewType(position: Int): Int {
        return innerAdapter.getItemViewType(position)
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
        innerAdapter.setHasStableIds(hasStableIds)
    }

    override fun getItemId(position: Int): Long {
        return innerAdapter.getItemId(position)
    }

    override fun onViewRecycled(holder: VH) {
        innerAdapter.onViewRecycled(holder)
    }

    override fun onFailedToRecycleView(holder: VH): Boolean {
        return innerAdapter.onFailedToRecycleView(holder)
    }

    override fun onViewAttachedToWindow(holder: VH) {
        innerAdapter.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: VH) {
        innerAdapter.onViewDetachedFromWindow(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        innerAdapter.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        innerAdapter.onDetachedFromRecyclerView(recyclerView)
    }

    override fun setStateRestorationPolicy(strategy: StateRestorationPolicy) {
        super.setStateRestorationPolicy(strategy)
        innerAdapter.stateRestorationPolicy = strategy
    }
}
