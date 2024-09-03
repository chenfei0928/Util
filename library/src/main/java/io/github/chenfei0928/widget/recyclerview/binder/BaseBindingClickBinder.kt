package io.github.chenfei0928.widget.recyclerview.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import io.github.chenfei0928.widget.recyclerview.adapter.ViewBindingHolder

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
