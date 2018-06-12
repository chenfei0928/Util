package com.chenfei.library.adapter

import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.chenfei.library.util.kotlin.DataBindingExt

abstract class CommonAdapter<Bean, Binding : ViewDataBinding>
protected constructor(
        @field:JvmField
        protected val mList: List<Bean>,
        @param:LayoutRes
        @field:LayoutRes
        private val layoutId: Int
) : RecyclerView.Adapter<ViewHolder<Binding>>(), MultiAdapterWrapper.InnerAdapter {

    override val viewTypeCount: Int
        get() = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<Binding> {
        val binding = DataBindingExt.inflate<Binding>(parent, layoutId)
        val holder = ViewHolder(binding)
        onViewHolderCreated(holder, binding)
        return holder
    }

    protected open fun onViewHolderCreated(holder: ViewHolder<Binding>, binding: Binding) {}

    override fun onBindViewHolder(holder: ViewHolder<Binding>, position: Int) {
        val bean = mList[position]
        holder.binding?.let {
            onBindViewHolder(holder, it, position, bean)
            onBindViewHolder(it, bean)
        }
    }

    protected abstract fun onBindViewHolder(binding: Binding, bean: Bean)

    protected open fun onBindViewHolder(holder: ViewHolder<Binding>, binding: Binding, position: Int, bean: Bean) {}

    override fun getItemCount(): Int = mList.size
}
