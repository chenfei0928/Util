package io.github.chenfei0928.widget.recyclerview.binding.part

import androidx.recyclerview.widget.RecyclerView
import io.github.chenfei0928.collection.removeRange
import io.github.chenfei0928.util.Log
import io.github.chenfei0928.widget.recyclerview.binding.AbsRecyclerViewBinding
import io.github.chenfei0928.widget.recyclerview.binding.RecyclerViewBindingUtil

/**
 * 局部可替换的 AbsRecyclerViewBinding 扩展支持。
 *
 * 提供局部刷新数据功能，对可刷新区块数据排序要求：
 * 1. title [ReplaceableSection.section]
 * 2. 展开后的listContent [ReplaceableSection.data]
 * 3. refresh [ReplaceableSection]
 *
 * 使用时建议在宿主 RecyclerViewBinding 内存放，并在其 RecyclerViewBinding 中注册[Replaceable]的binder。
 */
abstract class BaseReplaceableSpanBinding<Replaceable : ReplaceableSection<T>, T>(
    private val binding: AbsRecyclerViewBinding
) {

    /**
     * 更换区块内容，并通知适配器刷新，但有个要求，其数据排列方式必须是：
     * title[ReplaceableSection.section] - listContent[ReplaceableSection.data] - refresh[ReplaceableSection]
     * 否则在查找其正在显示的内容时会无法精准的查找到其内容范围（为兼容listContent只显示一部分而不是全部的使用用例情况，
     * 不根据[ReplaceableSection.data]的大小来移除项目，而是规定在listContent之后追加refresh刷新按钮的数据排列设计方案，
     * 如实际使用refresh按钮不在子列表之后而是在区块title上，也必须追加数据，使其view不可见方式以符合设计交互要求即可）。
     *
     * 数据更新时使用局部刷新，不使用[RecyclerView.Adapter.notifyDataSetChanged]，以达到局部刷新的样式
     */
    fun replaceSection(section: Replaceable, data: List<T>) {
        val list = RecyclerViewBindingUtil.getList(binding)
        // 不使用其data（bean的子列表）进行移除，而根据title与换一换的下标来找到子列表的范围以进行删除：
        // 其刷新返回的数据有可能会和列表已有数据内容重复，导致删除时删除错项目（removeAll方法）
        val firstBeanIndex = list.indexOf(section.section) + 1
        val lastBeanIndex = list.indexOf(section) - 1
        // 对找到的index进行校验，当下标未找到的时候不进行替换
        if (firstBeanIndex <= 0 || lastBeanIndex <= -2) {
            Log.w(TAG, "replaceSection: 替换区块时其sectionTitle、Replaceable数据在列表中未找到")
            return
        }
        // 从换一换倒序删除到第一个bean，简化逻辑并避免下标错误（downTo包含两极index）
        // 不要在此移除layoutParamRecord，notifyItemRangeInserted之后动画第一帧时仍然在展示旧的数据，需要其layoutParam
        list.removeRange(lastBeanIndex downTo firstBeanIndex)
        // 通知适配器从这个位置开始移除了多少个子项目
        val adapter = RecyclerViewBindingUtil.getAdapter(binding)
        adapter.notifyItemRangeRemoved(firstBeanIndex, lastBeanIndex - firstBeanIndex + 1)
        // 替换数据
        section.data = data
        val oldSize = list.size
        // 添加数据到列表展示
        replaceSectionContentDataImpl(firstBeanIndex, section)
        val replacedItemCount = list.size - oldSize
        // 通知适配器该范围内的项目已添加
        adapter.notifyItemRangeInserted(firstBeanIndex, replacedItemCount)
    }

    /**
     * 添加区块子内容数据到显示，只处理内部data即可，用于替换区块内容的实际实现。
     * 由于框架不知道子实现使用何种binder和布局方式，使用回调方式来实现实际添加内容的操作
     *
     * @param firstBeanIndex 要添加的数据的起始位置，用于[AbsRecyclerViewBinding.addListItems]
     *  或[AbsRecyclerViewBinding.addSingleItem]的position下标
     * @param section 被替换后的区块数据
     */
    protected abstract fun replaceSectionContentDataImpl(firstBeanIndex: Int, section: Replaceable)

    private companion object {
        private const val TAG = "KW_BaseReplaceableSpanB"
    }
}
