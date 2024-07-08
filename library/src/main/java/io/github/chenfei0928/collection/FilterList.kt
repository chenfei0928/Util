package io.github.chenfei0928.collection

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-12-03 14:42
 */
class FilterList<E>
/**
 *
 * @param E
 * @property list 源数据集合
 * @property realtimeFilter 实时过滤，如果为true时每次访问该列表时都会实时对数据进行过滤。
 *                          false时将会对数据源进行过滤后缓存，访问时调用缓存数据。
 * @constructor
 *
 * @param filterPredicate
 */
constructor(
    val list: MutableList<E>, private val realtimeFilter: Boolean, filterPredicate: (E) -> Boolean
) : MutableList<E> by list {
    private var filteredList: List<E> = list.filter(filterPredicate)
        get() = if (realtimeFilter) {
            list.filter(filterPredicate)
        } else {
            field
        }

    var filterPredicate: (E) -> Boolean = filterPredicate
        set(value) {
            field = value
            notifyFilterOrDataSourceUpdate()
        }

    fun notifyFilterOrDataSourceUpdate() {
        require(!realtimeFilter) { "实时过滤的FilterList不需要通知数据源已更新" }
        filteredList = list.filter(filterPredicate)
    }

    override val size: Int
        get() = filteredList.size

    override fun get(index: Int): E {
        return filteredList[index]
    }

    override fun indexOf(element: E): Int {
        return filteredList.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return list.none(filterPredicate)
    }

    override fun iterator(): MutableIterator<E> {
        return listIterator(0)
    }

    override fun lastIndexOf(element: E): Int {
        return filteredList.lastIndexOf(element)
    }

    override fun add(index: Int, element: E) {
        when (index) {
            0 -> {
                list.add(0, element)
            }
            1 -> {
                list.add(1, element)
            }
            else -> {
                list.add(toRealIndex(index, true), element)
            }
        }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        return when (index) {
            0 -> {
                list.addAll(0, elements)
            }
            1 -> {
                list.addAll(1, elements)
            }
            else -> {
                list.addAll(toRealIndex(index, true), elements)
            }
        }
    }

    override fun listIterator(): MutableListIterator<E> {
        return listIterator(0)
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        val realIndex = toRealIndex(index, false)
        val orgIterator = list.listIterator(realIndex)
        return object : MutableListIterator<E> by orgIterator {
            private var next: E? = null
            private var pre: E? = null

            override fun hasPrevious(): Boolean {
                if (pre != null) {
                    return true
                }
                while (orgIterator.hasPrevious()) {
                    val pre = orgIterator.previous()
                    if (filterPredicate(pre)) {
                        this.pre = pre
                        return true
                    }
                }
                return false
            }

            override fun nextIndex(): Int {
                return indexOf(next)
            }

            override fun previous(): E {
                val pre = pre
                this.pre = null
                return pre!!
            }

            override fun previousIndex(): Int {
                return indexOf(pre)
            }

            override fun hasNext(): Boolean {
                if (next != null) {
                    return true
                }
                while (orgIterator.hasNext()) {
                    val next = orgIterator.next()
                    if (filterPredicate(next)) {
                        this.next = next
                        return true
                    }
                }
                return false
            }

            override fun next(): E {
                val next = next
                this.next = null
                return next!!
            }
        }
    }

    override fun removeAt(index: Int): E {
        return get(index).also {
            remove(it)
        }
    }

    override fun set(index: Int, element: E): E {
        return list.set(toRealIndex(index, false), element)
    }

    fun replace(currentElement: E, newElement: E): E {
        return list.set(list.indexOf(currentElement), newElement)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        val fromRealIndex = toRealIndex(fromIndex, false)
        val realToIndex = toRealIndex(toIndex, false)
        return FilterList(list.subList(fromRealIndex, realToIndex), realtimeFilter, filterPredicate)
    }

    private fun toRealIndex(index: Int, neighbor: Boolean): Int {
        return when (index) {
            0 -> 0
            size -> list.size
            else -> list.indexOf(list.filter(filterPredicate)[index - if (neighbor) 1 else 0])
        }
    }
}

fun <E> MutableList<E>.toFilterList(
    realtimeFilter: Boolean = true, predicate: (E) -> Boolean
): FilterList<E> {
    return FilterList(this, realtimeFilter, predicate)
}
