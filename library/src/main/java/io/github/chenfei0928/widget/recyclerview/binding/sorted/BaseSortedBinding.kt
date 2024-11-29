package io.github.chenfei0928.widget.recyclerview.binding.sorted

import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import com.drakeet.multitype.MultiTypeAdapter
import io.github.chenfei0928.collection.SortedListList
import io.github.chenfei0928.reflect.parameterized.getParentParameterizedTypeClassDefinedImplInChild
import io.github.chenfei0928.widget.recyclerview.adapter.IMultiTypeAdapterStringer

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-09-19 10:54
 */
abstract class BaseSortedBinding<E : Any>(
    eClass: Class<E>? = null,
    protected val adapter: MultiTypeAdapter = IMultiTypeAdapterStringer.IMultiTypeAdapter()
) {
    /**
     * 自排序列表，其会在内容更新后自动更新[adapter]
     */
    protected val list: SortedList<E> = SortedList(
        eClass ?: getParentParameterizedTypeClassDefinedImplInChild(0),
        object : SortedListAdapterCallback<E>(adapter) {
            override fun compare(o1: E, o2: E): Int {
                return this@BaseSortedBinding.compare(o1, o2)
            }

            override fun areContentsTheSame(oldItem: E, newItem: E): Boolean {
                return this@BaseSortedBinding.areContentsTheSame(oldItem, newItem)
            }

            override fun areItemsTheSame(item1: E, item2: E): Boolean {
                return this@BaseSortedBinding.areItemsTheSame(item1, item2)
            }
        }
    )

    init {
        adapter.items = SortedListList(list)
        if (adapter is IMultiTypeAdapterStringer) {
            adapter.binding = this
        }
    }

    // -1将会排序在列表前部，1将会排序在列表后部
    protected abstract fun compare(o1: E, o2: E): Int
    protected open fun areContentsTheSame(oldItem: E, newItem: E): Boolean {
        return oldItem == newItem
    }

    protected abstract fun areItemsTheSame(item1: E, item2: E): Boolean
}
