package com.chenfei.util.recyclerview

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.drakeet.multitype.MultiTypeAdapter
import com.chenfei.adapter.DelegateAdapterDataObserver
import com.chenfei.collection.RecyclerViewAdapterDataSource
import com.chenfei.util.recyclerview.grid.BaseGridSpanRecyclerViewBinderRecorderBinding
import com.chenfei.util.recyclerview.grid.BaseGridSpanRecyclerViewBinding

/**
 * 为[RecyclerView]提供数据填充的viewBinding工具类。
 * 用于管理需要显示的原始数据和适配器，并提供简单的添加和删除数据操作，
 * 不提供/设置[LayoutManager]，布局方式由子类或使用处设置
 *
 * 建议用于[LinearLayoutManager]或其它[LayoutManager]、无需一bean类型对多binder的场景
 *
 * 关于各个子类使用建议：
 * [GridLayoutManager]、一bean类型对多binder --> [BaseGridSpanRecyclerViewBinderRecorderBinding]；
 * [GridLayoutManager] --> [BaseGridSpanRecyclerViewBinding]；
 * [LinearLayoutManager]或其它[LayoutManager]、一bean类型对多binder --> [BaseRecyclerViewBinderRecorderBinding]（需初始化时设置[LayoutManager]）；
 * [LinearLayoutManager]或其它[LayoutManager] --> [AbsRecyclerViewBinding]（需初始化时设置[LayoutManager]）。
 *
 * 进行额外扩展时（支持其它[LayoutManager]）的父类建议：
 * 需要每个item有额外属性配置、一bean类型对多binder --> [AbsRecyclerViewBinderRecorderBinding]；
 * 需要每个item有额外属性配置 --> [AbsLayoutParamRecyclerViewBinding]；
 * 无以上需求的基础扩展 --> [AbsRecyclerViewBinding]。
 */
abstract class AbsRecyclerViewBinding(
    contentView: RecyclerView, list: MutableList<Any> = arrayListOf()
) {
    private val _list: MutableList<Any> = list

    /**
     * 暴露给子类查找，禁止直接对其add，
     * 添加操作使用本类提供的[addSingleItem]、[addListItems]进行添加操作
     */
    protected val list: List<Any> = _list
    protected val adapter = MultiTypeAdapter(_list).apply {
        if (_list is RecyclerViewAdapterDataSource) {
            _list.adapterDataObserver = DelegateAdapterDataObserver(this)
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
            _list.addAll(data)
        } else {
            _list.addAll(position, data)
        }
    }

    /**
     * 添加单个的条目，非视频或金句列表
     *
     * @param item        要添加的条目item，其类型要事先在该类初始化方法中添加至adapter中
     */
    protected open fun addSingleItem(position: Int = list.size, item: Any) {
        if (position == list.size) {
            _list.add(item)
        } else {
            _list.add(position, item)
        }
    }

    /**
     * 移除最后加入的一项
     */
    protected open fun removeLast(): Any {
        return _list.removeAt(list.lastIndex)
    }

    protected open fun clear() {
        _list.clear()
    }
}
