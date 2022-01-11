package io.github.chenfei0928.widget.recyclerview.binding

import android.view.View
import androidx.collection.SystemIdentityArrayMap
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.holder
import io.github.chenfei0928.widget.recyclerview.adapter.RecyclerViewHolderModelProvider
import io.github.chenfei0928.util.Log
import io.github.chenfei0928.widget.recyclerview.binding.AbsLayoutParamRecyclerViewBinding.LayoutParams

/**
 * 支持[LayoutParams]的RecyclerViewBinding。
 *
 * 对[AbsRecyclerViewBinding]进行了添加[LayoutParams]支持，以允许存储item的扩展属性。
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-19 16:18
 */
abstract class AbsLayoutParamRecyclerViewBinding<LP : LayoutParams>(
    contentView: RecyclerView, list: MutableList<Any> = arrayListOf()
) : AbsRecyclerViewBinding(contentView, list) {
    /**
     * 列表头大小，如果子类使用了通过adapterWrapper方式提供了额外的列表头，
     * 该列表头会影响真实展示的内容的adapterPosition，需要设置其列表头大小以计算其在该框架内数据的位置
     */
    protected var adapterWrapperHeaderSize = 0

    private val _layoutParamsRecord = SystemIdentityArrayMap<Any, LP>()

    /**
     * 记录某个bean的布局信息
     * 提供给子类查找，禁止直接对其add，
     * 添加操作使用本类提供的[addSingleItem]、[addListItems]进行添加操作
     */
    protected val layoutParamsRecord: Map<Any, LP> = _layoutParamsRecord

    /**
     * 使用在[RecyclerView]中的子视图获取指定显示内容的布局参数
     */
    internal fun getLayoutParams(view: View): LP? {
        val lp = view.layoutParams as RecyclerView.LayoutParams
        // 先检查viewHolder，如果viewHolder内保存了显示的数据，这直接使用其数据来获取布局参数
        val holder = lp.holder
        if (holder is RecyclerViewHolderModelProvider<*>) {
            return _layoutParamsRecord[holder.item]
        }
        val position = lp.bindingAdapterPosition
        // 如果在header或footer内，span占满
        if (position < adapterWrapperHeaderSize || position >= adapterWrapperHeaderSize + list.size) {
            Log.d(TAG, "getItemOffsets: $position ${lp.holder} layoutParams not found")
            return null
        }
        // 根据其在RecyclerView中的位置，找到其显示的bean实例
        val any = list[position - adapterWrapperHeaderSize]
        // 获取其布局参数信息
        Log.d(TAG, "getItemOffsets: $position $any layoutParams not found")
        return _layoutParamsRecord[any]
    }

    /**
     * 生成布局参数，如果之类扩展了自己的布局参数[LP]，也要提供一个用于生成子类布局参数的接口
     */
    protected open fun generateDefaultLayoutParams(): LP {
        return LayoutParams() as LP
    }

    @Deprecated(
        level = DeprecationLevel.HIDDEN,
        replaceWith = ReplaceWith(expression = "addListItems(position, data)"),
        message = "使用带布局参数的接口来添加"
    )
    final override fun <E : Any> addListItems(position: Int, data: List<E>) {
        val layoutParams = generateDefaultLayoutParams()
        addListItems(position, data) { _, _ -> layoutParams }
    }

    /**
     * 追加整个集合，使用一致的边距与span配置
     */
    protected open fun <E : Any> addListItems(
        position: Int = list.size,
        data: List<E>,
        layoutParamsGenerator: (index: Int, item: E) -> LP = { _, _ -> generateDefaultLayoutParams() }
    ) {
        super.addListItems(position, data)
        // 记录边距、span
        data.forEachIndexed { index, any ->
            _layoutParamsRecord[any] = layoutParamsGenerator(index, any)
        }
    }

    @Deprecated(level = DeprecationLevel.HIDDEN, message = "使用带布局参数的接口来添加")
    final override fun addSingleItem(position: Int, item: Any) {
        addSingleItem(position, item, generateDefaultLayoutParams())
    }

    /**
     * 添加单个的条目，非视频或金句列表
     *
     * @param item        要添加的条目item，其类型要事先在该类初始化方法中添加至adapter中
     * @param layoutParams 布局显示参数
     */
    protected open fun addSingleItem(
        position: Int = list.size, item: Any, layoutParams: LP = generateDefaultLayoutParams()
    ) {
        super.addSingleItem(position, item)
        // 记录边距、span
        _layoutParamsRecord[item] = layoutParams
    }

    @Deprecated(level = DeprecationLevel.HIDDEN, message = "使用[removeLast(Boolean)]移除")
    override fun removeLast(): Any {
        return removeLast(false)
    }

    /**
     * 移除最后加入的一项。
     * 但使用时要留意如果最后一项的数据是作为单例或在列表中使用了多次时，不能移除其布局参数，
     * 否则会在其他地方显示其数据时无法正常使用布局参数。
     * 例如：
     * 用于处理单例占位符作为split或space时其可能会在列表中使用并展示多次的情况下，
     * 其虽然要移除作为最后一项，但其他位置还要使用布局参数而不能移除布局参数的情况。
     *
     * @param removeLayoutParams 同时移除其布局参数，如果最后一项的数据使用了不止一次，此处要设置为false
     */
    protected fun removeLast(removeLayoutParams: Boolean = true): Any {
        val last = super.removeLast()
        if (removeLayoutParams) {
            _layoutParamsRecord.remove(last)
        }
        return last
    }

    override fun clear() {
        _layoutParamsRecord.clear()
        super.clear()
    }

    open class LayoutParams

    companion object {
        private const val TAG = "KW_AbsLpRecyclerViewB"
    }
}
