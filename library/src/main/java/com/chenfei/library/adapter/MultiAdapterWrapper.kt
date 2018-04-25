package com.chenfei.library.adapter

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.changeViewType
import android.view.ViewGroup
import com.chenfei.library.R

/**
 * Created by MrFeng on 2018/1/8.
 */
open class MultiAdapterWrapper<in Adapter, ViewHolder : RecyclerView.ViewHolder>
    : RecyclerView.Adapter<ViewHolder>()
        where Adapter : RecyclerView.Adapter<ViewHolder>, Adapter : MultiAdapterWrapper.InnerAdapter {
    private val adapterId = R.id.multiAdapterId_adapter
    private val realViewTypeId = R.id.multiAdapterId_realViewType
    private val mAdapters: MutableList<Adapter> = ArrayList()

    fun add(adapter: Adapter) {
        mAdapters.add(adapter)
    }

    override fun getItemCount(): Int {
        return mAdapters.sumBy { it.itemCount }
    }

    override fun getItemViewType(position: Int): Int {
        @Suppress("NAME_SHADOWING")
        var position = position
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
        @Suppress("NAME_SHADOWING")
        var viewType = viewType
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
        throw IllegalArgumentException("viewType is $viewType but can not find match ViewHolder")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemViewType = holder.itemViewType
        changeViewType(holder, holder.realViewType)
        val upOfThisAdapter = mAdapters
                .takeWhile { it != holder.adapter }
                .sumBy { it.itemCount }
        holder.adapter.onBindViewHolder(holder, position - upOfThisAdapter)
        changeViewType(holder, itemViewType)
    }

    interface InnerAdapter {
        val viewTypeCount: Int
    }

    @Suppress("UNCHECKED_CAST")
    private var ViewHolder.adapter: Adapter
        get() = itemView.getTag(adapterId) as Adapter
        set(value) {
            itemView.setTag(adapterId, value)
        }
    @Suppress("UNCHECKED_CAST")
    private var ViewHolder.realViewType: Int
        get() = itemView.getTag(realViewTypeId) as Int
        set(value) {
            itemView.setTag(realViewTypeId, value)
        }
}
