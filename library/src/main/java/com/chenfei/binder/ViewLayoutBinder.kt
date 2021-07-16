package com.chenfei.binder

import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.drakeet.multitype.ItemViewBinder
import com.chenfei.adapter.ViewBindingHolder
import com.chenfei.adapter.ViewHolder
import com.chenfei.util.ExecutorUtil

/**
 * User: ChenFei(chenfei0928@gmail.com)
 * Date: 2019-03-21
 * Time: 17:05
 */
abstract class BaseViewHolderLayoutBinder<T, VH : ViewHolder<T>> : ItemViewBinder<T, VH>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): VH {
        val holder = onCreateViewHolderImpl(inflater, parent)
        // 检查是否是执行在ui线程
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onViewHolderCreated(holder, parent)
        } else {
            ExecutorUtil.postToUiThread {
                onViewHolderCreated(holder, parent)
            }
        }
        return holder
    }

    abstract fun onCreateViewHolderImpl(inflater: LayoutInflater, parent: ViewGroup): VH

    open fun onViewHolderCreated(holder: VH, parent: ViewGroup) {}

    override fun onBindViewHolder(holder: VH, item: T, payloads: List<Any>) {
        holder.item = item
        super.onBindViewHolder(holder, item, payloads)
    }
}

abstract class BaseBindingBinder<T, V : ViewBinding>(
    private val viewBindingInflater: (LayoutInflater, ViewGroup, Boolean) -> V
) : BaseViewHolderLayoutBinder<T, ViewBindingHolder<T, V>>() {

    final override fun onCreateViewHolderImpl(
        inflater: LayoutInflater, parent: ViewGroup
    ): ViewBindingHolder<T, V> {
        return ViewBindingHolder(viewBindingInflater(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewBindingHolder<T, V>, item: T) {
        onBindViewHolder(holder, holder.viewBinding, item)
    }

    open fun onBindViewHolder(holder: ViewBindingHolder<T, V>, viewBinding: V, item: T) {}
}

abstract class BaseBindingClickBinder<T, V : ViewBinding>(
    viewBindingInflater: (LayoutInflater, ViewGroup, Boolean) -> V
) : BaseBindingBinder<T, V>(viewBindingInflater), ClickableBinder<T> {

    override fun onViewHolderCreated(holder: ViewBindingHolder<T, V>, parent: ViewGroup) {
        super.onViewHolderCreated(holder, parent)
        // 初始化时设置监听器
        holder.itemView.setOnClickListener {
            holder.item?.let {
                onItemClick(it)
            }
        }
    }
}

interface ClickableBinder<T> {
    fun onItemClick(item: T)
}
