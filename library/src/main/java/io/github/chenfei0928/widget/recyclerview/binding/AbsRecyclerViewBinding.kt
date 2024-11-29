package io.github.chenfei0928.widget.recyclerview.binding

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.drakeet.multitype.MultiTypeAdapter
import io.github.chenfei0928.collection.RecyclerViewAdapterDataObserverMultiList
import io.github.chenfei0928.collection.RecyclerViewAdapterDataSource
import io.github.chenfei0928.widget.recyclerview.adapter.DelegateAdapterDataObserver
import io.github.chenfei0928.widget.recyclerview.adapter.IMultiTypeAdapterStringer
import io.github.chenfei0928.widget.recyclerview.binding.grid.BaseGridSpanRecyclerViewBinderRecorderBinding
import io.github.chenfei0928.widget.recyclerview.binding.grid.BaseGridSpanRecyclerViewBinding

/**
 * 为[RecyclerView]提供数据填充的viewBinding工具类。
 * 用于管理需要显示的原始数据和适配器，并提供简单的添加和删除数据操作，
 * 不提供/设置[LayoutManager]，布局方式由子类或使用处设置
 *
 * 建议用于[LinearLayoutManager]或其它[LayoutManager]、无需一bean类型对多binder的场景
 *
 * 关于各个子类使用建议：
 * - [GridLayoutManager]、一bean类型对多binder --> [BaseGridSpanRecyclerViewBinderRecorderBinding]；
 * - [GridLayoutManager]、一bean类型对一binder --> [BaseGridSpanRecyclerViewBinding]；
 * - [LinearLayoutManager]或其它[LayoutManager]、一bean类型对多binder -->
 * [BaseRecyclerViewBinderRecorderBinding]（需初始化时设置[LayoutManager]）；
 * - [LinearLayoutManager]或其它[LayoutManager]、一bean类型对一binder -->
 * [AbsRecyclerViewBinding]（需初始化时设置[LayoutManager]）。
 *
 * 进行额外扩展时（支持其它[LayoutManager]）的父类建议：
 * - 需要每个item有额外属性配置、一bean类型对多binder --> [AbsRecyclerViewBinderRecorderBinding]；
 * - 需要每个item有额外属性配置、一bean类型对一binder --> [AbsLayoutParamRecyclerViewBinding]；
 * - 无以上需求的基础扩展 --> [AbsRecyclerViewBinding]。
 *
 * @param list 内容列表，考虑内容列表插入顺序等于最终顺序，所以不使用自刷新集合类
 * [RecyclerViewAdapterDataObserverMultiList]，但需要子类调用处在数据插入完成后
 * 自行调用[adapter]的对应方法更新列表。但如果传入的[list]是[RecyclerViewAdapterDataSource]的子类，
 * 将会对其更新[RecyclerViewAdapterDataSource.adapterDataObserver]。
 */
abstract class AbsRecyclerViewBinding(
    contentView: RecyclerView, list: MutableList<Any> = arrayListOf()
) {

    /**
     * 暴露给子类查找，禁止直接对其add，
     * 添加操作使用本类提供的[addSingleItem]、[addListItems]进行添加操作
     */
    protected val list: List<Any>
        private field: MutableList<Any> = list
    protected val adapter = IMultiTypeAdapterStringer.IMultiTypeAdapter(list).apply {
        binding = this@AbsRecyclerViewBinding
        if (list is RecyclerViewAdapterDataSource) {
            list.adapterDataObserver = DelegateAdapterDataObserver(this)
        }
    }

    init {
        // 适配器
        contentView.adapter = adapter
    }

    /**
     * 追加整个集合
     */
    protected open fun <E : Any> addListItems(position: Int = list.size, data: List<E>) {
        if (position == list.size) {
            list.addAll(data)
        } else {
            list.addAll(position, data)
        }
    }

    /**
     * 添加单个的条目，非视频或金句列表
     *
     * @param item        要添加的条目item，其类型要事先在该类初始化方法中添加至adapter中
     */
    protected open fun addSingleItem(position: Int = list.size, item: Any) {
        if (position == list.size) {
            list.add(item)
        } else {
            list.add(position, item)
        }
    }

    /**
     * 移除最后加入的一项
     */
    protected open fun removeLast(): Any {
        return list.removeAt(list.lastIndex)
    }

    protected open fun clear() {
        list.clear()
    }

    protected open val partAccessor: BindingAccessor = object : BindingAccessor {
        override val adapter: MultiTypeAdapter
            get() = this@AbsRecyclerViewBinding.adapter
        override val list: MutableList<Any>
            get() = this@AbsRecyclerViewBinding.list

        override fun addSingleItem(position: Int, item: Any) {
            this@AbsRecyclerViewBinding.addSingleItem(position, item)
        }
    }
}
