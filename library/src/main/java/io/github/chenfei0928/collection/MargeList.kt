package io.github.chenfei0928.collection

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-05-10 18:32
 */
class MargeList<E>(
    private vararg val lists: List<E>
) : AbstractList<E>() {
    override val size: Int
        get() = lists.sumOf { it.size }

    override fun get(index: Int): E {
        var currentIndex = index
        lists.forEach {
            if (it.size > currentIndex) {
                return it[currentIndex]
            } else {
                currentIndex -= it.size
            }
        }
        throw IndexOutOfBoundsException("Index: $index, Size: $size")
    }
}
