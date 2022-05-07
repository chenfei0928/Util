package io.github.chenfei0928.collection

import androidx.recyclerview.widget.RecyclerView

/**
 * 内容变更时会自动通知RecyclerViewAdapter的List
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-09-16 16:16
 */
open class RecyclerViewAdapterDataObserverMultiList<E>(
    private val list: MutableList<E> = ArrayList()
) : MutableList<E> by list, RecyclerView.AdapterDataObserver(), RecyclerViewAdapterDataSource {
    override var adapterDataObserver: RecyclerView.AdapterDataObserver? = null

    override fun add(element: E): Boolean {
        return if (list.add(element)) {
            adapterDataObserver?.onItemRangeInserted(list.indexOf(element), 1)
            true
        } else {
            false
        }
    }

    override fun add(index: Int, element: E) {
        list.add(index, element)
        adapterDataObserver?.onItemRangeInserted(index, 1)
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        return if (list.addAll(index, elements)) {
            adapterDataObserver?.onItemRangeInserted(index, elements.size)
            true
        } else {
            false
        }
    }

    override fun addAll(elements: Collection<E>): Boolean {
        val oldSize = list.size
        return if (list.addAll(elements)) {
            adapterDataObserver?.onItemRangeInserted(oldSize, elements.size)
            true
        } else {
            false
        }
    }

    fun clearToAddAll(elements: Collection<E>) {
        list.clear()
        list.addAll(elements)
        adapterDataObserver?.onChanged()
    }

    override fun clear() {
        val oldSize = list.size
        if (oldSize == 0) {
            return
        }
        list.clear()
        adapterDataObserver?.onItemRangeRemoved(0, oldSize)
    }

    override fun remove(element: E): Boolean {
        val indexOf = list.indexOf(element)
        return if (list.remove(element)) {
            adapterDataObserver?.onItemRangeRemoved(indexOf, 1)
            true
        } else {
            false
        }
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        return list.removeAll(elements)
    }

    override fun removeAt(index: Int): E {
        return list
            .removeAt(index)
            .also {
                adapterDataObserver?.onItemRangeRemoved(index, 1)
            }
    }

    override fun set(index: Int, element: E): E {
        return list
            .set(index, element)
            .apply {
                adapterDataObserver?.onItemRangeChanged(index, 1, null)
            }
    }
}
