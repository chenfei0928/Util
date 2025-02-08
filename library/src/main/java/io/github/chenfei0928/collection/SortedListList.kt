package io.github.chenfei0928.collection

import androidx.recyclerview.widget.SortedList

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-08-24 17:29
 */
class SortedListList<E>(
    private val impl: SortedList<E>,
) : AbstractList<E>() {

    override val size: Int
        get() = impl.size()

    override fun get(index: Int): E {
        @Suppress("kotlin:S6518")
        return impl.get(index)
    }

    override fun indexOf(element: E): Int {
        return impl.indexOf(element)
    }
}
