package com.chenfei.util.recyclerview

import androidx.recyclerview.widget.RecyclerView

/**
 * 局部可展开的 AbsRecyclerViewBinding 扩展支持
 * 提供局部展开数据功能，对可展开区块展开时将会将可展开区块移除并将展开后的数据填充。
 * 使用时建议在宿主 RecyclerViewBinding 内存放，并在其 RecyclerViewBinding 中注册[Collapsed]的binder
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-19 17:37
 */
abstract class BaseCollapsingSpanBinding<Collapsed, Expanded>(
    private val binding: AbsRecyclerViewBinding
) {

    /**
     * 展开区块内容，将被展开的区块移除后，通知适配器刷新
     *
     * 数据更新时使用局部刷新，不使用[RecyclerView.Adapter.notifyDataSetChanged]，以达到局部刷新的样式
     */
    fun expandSection(section: Collapsed, data: Expanded) {
        val list = RecyclerViewBindingUtil.getList(binding)
        val adapter = RecyclerViewBindingUtil.getAdapter(binding)
        // 可展开区块的位置
        val collapsedIndex = list.indexOf(section)
        list.removeAt(collapsedIndex)
        // 通知适配器从这个位置开始移除了被展开区块
        adapter.notifyItemRemoved(collapsedIndex)
        val oldSize = list.size
        // 添加数据到列表展示
        expandSectionContentDataImpl(collapsedIndex, data)
        val expandedItemCount = list.size - oldSize
        // 通知适配器该范围内的项目已添加
        adapter.notifyItemRangeInserted(collapsedIndex, expandedItemCount)
    }

    /**
     * 添加已展开的区块子内容数据到显示，只处理内部data即可，用于展开被折叠区块内容的实际实现。
     * 由于框架不知道子实现使用何种binder和布局方式，使用回调方式来实现实际添加内容的操作
     *
     * @param firstBeanIndex 要添加的数据的起始位置，用于[AbsRecyclerViewBinding.addListItems]
     *  或[AbsRecyclerViewBinding.addSingleItem]的position下标
     * @param section 被展开的区块数据
     */
    protected abstract fun expandSectionContentDataImpl(firstBeanIndex: Int, section: Expanded)
}
