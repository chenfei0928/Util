package io.github.chenfei0928.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerViewHelper
import io.github.chenfei0928.util.R
import io.github.chenfei0928.widget.recyclerview.ViewHolderTagDelegate

/**
 * Created by MrFeng on 2018/1/8.
 */
open class MultiAdapterWrapper<in Adapter, ViewHolder : RecyclerView.ViewHolder>(
    adapters: MutableList<Adapter> = ArrayList()
) : RecyclerView.Adapter<ViewHolder>() where Adapter : RecyclerView.Adapter<ViewHolder>, Adapter : MultiAdapterWrapper.InnerAdapter {
    private val mAdapters: MutableList<Adapter> = adapters

    fun add(adapter: Adapter) {
        mAdapters.add(adapter)
    }

    override fun getItemCount(): Int {
        return mAdapters.sumOf { it.itemCount }
    }

    override fun getItemViewType(position: Int): Int {
        @Suppress("NAME_SHADOWING") var position = position
        var skippedViewType = 0
        for (adapter in mAdapters) {
            val itemCount = adapter.itemCount
            if (itemCount <= position) {
                skippedViewType += adapter.viewTypeCount
                position -= itemCount
            } else {
                return adapter.getItemViewType(position) + skippedViewType
            }
        }
        return skippedViewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        @Suppress("NAME_SHADOWING") var viewType = viewType
        for (adapter in mAdapters) {
            val typeCount = adapter.viewTypeCount
            if (typeCount <= viewType) {
                viewType -= typeCount
            } else {
                val viewHolder = adapter.onCreateViewHolder(parent, viewType)
                viewHolder.adapter = adapter
                viewHolder.realViewType = viewType
                return viewHolder
            }
        }
        throw IllegalArgumentException("viewType is $viewType but can not find match DataBindingViewHolder")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemViewType = holder.itemViewType
        RecyclerViewHelper.changeViewType(holder, holder.realViewType!!)
        val upOfThisAdapter = mAdapters
            .takeWhile { it != holder.adapter }
            .sumOf { it.itemCount }
        holder.adapter?.onBindViewHolder(holder, position - upOfThisAdapter)
        RecyclerViewHelper.changeViewType(holder, itemViewType)
    }

    interface InnerAdapter {
        val viewTypeCount: Int
    }

    private var ViewHolder.adapter: Adapter? by ViewHolderTagDelegate(R.id.multiAdapterId_adapter)

    private var ViewHolder.realViewType: Int? by ViewHolderTagDelegate(R.id.multiAdapterId_realViewType)
}
