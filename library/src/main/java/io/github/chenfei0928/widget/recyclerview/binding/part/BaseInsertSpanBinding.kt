package io.github.chenfei0928.widget.recyclerview.binding.part

import io.github.chenfei0928.widget.recyclerview.binding.AbsRecyclerViewBinding
import io.github.chenfei0928.widget.recyclerview.binding.BindingAccessor

/**
 * 局部追加/加载更多的 AbsRecyclerViewBinding 扩展支持。
 *
 * 自行实现追加操作的“加载更多”按钮binder，除必须获知追加数据的插入位置外与其所属section外，对其binder数据无任何要求。
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-10-13 22:16
 */
abstract class BaseInsertSpanBinding<Section, InsertData>(
    private val binding: BindingAccessor
) {
    /**
     * 追加区块内容
     *
     * 数据更新时使用局部刷新，不使用[RecyclerView.Adapter.notifyDataSetChanged]，以达到局部刷新的样式
     */
    fun insertSectionContentData(firstBeanIndex: Int, section: Section, data: InsertData) {
        val list = binding.list
        val adapter = binding.adapter
        // 通知适配器从这个位置开始移除了被展开区块
        val oldSize = list.size
        // 添加数据到列表展示
        insertSectionContentDataImpl(firstBeanIndex, section, data)
        val expandedItemCount = list.size - oldSize
        // 通知适配器该范围内的项目已添加
        adapter.notifyItemRangeInserted(firstBeanIndex, expandedItemCount)
    }

    /**
     * 追加区块子内容数据到显示，只处理内部data即可，用于局部追加/加载更多区块内容的实际实现。
     * 由于框架不知道子实现使用何种binder和布局方式，使用回调方式来实现实际添加内容的操作
     *
     * @param firstBeanIndex 要添加的数据的起始位置，用于[AbsRecyclerViewBinding.addListItems]
     *  或[AbsRecyclerViewBinding.addSingleItem]的position下标
     * @param data 被追加的区块数据
     */
    protected abstract fun insertSectionContentDataImpl(
        firstBeanIndex: Int, section: Section, data: InsertData
    )
}
