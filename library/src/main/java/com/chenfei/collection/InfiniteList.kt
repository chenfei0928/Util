package com.chenfei.collection

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-05-09 17:16
 */
class InfiniteList<E>(
    private val list: List<E>
) : List<E> by list {

    override val size: Int
        get() = if (isEmpty()) 0 else Int.MAX_VALUE

    val realSize: Int
        get() = list.size

    override fun get(index: Int): E = list[index % list.size]
}
