package com.drakeet.multitype

import io.github.chenfei0928.adapter.ViewHolder

/**
 * 检查指定 viewHolder 是否显示的是该数据
 * 如果是，直接重绑定 viewHolder
 * 如果指定 viewHolder 显示的数据已被刷新，则通过数据查找其位置并刷新
 *
 * @param holder 在监听器中进行点击操作的 viewHolder
 * @param item   用户点击时的展示数据 item
 * @param rebindBlock 仅当指定 viewHolder 所展示的结构体实例未更新时，调用该回调，进行更新 viewHolder 显示内容
 */
inline fun <T : Any, VH : ViewHolder<T>> ItemViewBinder<T, VH>.notifyItemChanged(
    holder: VH, item: T, rebindBlock: VH.() -> Unit
) {
    if (holder.item == item) {
        rebindBlock(holder)
    } else {
        adapter.notifyItemChanged(item)
    }
}

fun <T : Any, VH : ViewHolder<T>> ItemViewBinder<T, VH>.notifyItemChanged(holder: VH, item: T) {
    notifyItemChanged(holder, item) {
        onBindViewHolder(holder, item, emptyList())
    }
}
