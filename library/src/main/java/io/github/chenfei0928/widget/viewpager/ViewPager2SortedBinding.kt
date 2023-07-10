package io.github.chenfei0928.widget.viewpager

import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import androidx.viewpager2.widget.ViewPager2
import com.drakeet.multitype.MultiTypeAdapter
import io.github.chenfei0928.collection.SortedListList
import io.github.chenfei0928.reflect.parameterized.getParentParameterizedTypeClassDefinedImplInChild

/**
 * @author chenf()
 * @date 2023-03-02 15:11
 */
abstract class ViewPager2SortedBinding<E : Any>(
    contentView: ViewPager2,
    protected val adapter: MultiTypeAdapter = MultiTypeAdapter()
) {
    private val klass: Class<E> = getParentParameterizedTypeClassDefinedImplInChild(0)
    protected val list: SortedList<E>

    private val callback: SortedList.Callback<E> = object : SortedListAdapterCallback<E>(adapter) {
        override fun compare(o1: E, o2: E): Int {
            return this@ViewPager2SortedBinding.compare(o1, o2)
        }

        override fun areContentsTheSame(oldItem: E, newItem: E): Boolean {
            return this@ViewPager2SortedBinding.areContentsTheSame(oldItem, newItem)
        }

        override fun areItemsTheSame(item1: E, item2: E): Boolean {
            return this@ViewPager2SortedBinding.areItemsTheSame(item1, item2)
        }
    }

    init {
        list = SortedList(klass, callback)
        val listList = SortedListList(list)
        adapter.items = listList
        contentView.adapter = adapter
    }

    protected abstract fun compare(o1: E, o2: E): Int
    protected open fun areContentsTheSame(oldItem: E, newItem: E): Boolean {
        return oldItem == newItem
    }

    protected abstract fun areItemsTheSame(item1: E, item2: E): Boolean
}
