package io.github.chenfei0928.collection

import java.util.AbstractList

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-20 17:50
 */
class MapKeyValueList(
    private val map: Map<*, List<*>>
) : AbstractList<Any>() {
    override val size: Int
        get() {
            var count = 0
            map.values.forEach {
                if (it.isNotEmpty()) {
                    count++
                    count += it.size
                }
            }
            return count
        }

    override fun get(index: Int): Any {
        var indexI = index
        map.forEach { (k, v) ->
            if (v.isNotEmpty()) {
                if (indexI == 0) {
                    return k as Any
                }
                indexI--
                if (indexI < v.size) {
                    return v[indexI] as Any
                }
                indexI -= v.size
            }
        }
        throw IndexOutOfBoundsException("size is $size but index is $index $this")
    }

    @Suppress("ReturnCount")
    override fun indexOf(element: Any?): Int {
        var index = 0
        map.forEach { (k, v) ->
            if (v.isNotEmpty()) {
                if (k == element) {
                    return index
                }
                index++
                val indexOf = v.indexOf(element)
                if (indexOf > 0) {
                    return indexOf + index
                }
                index += indexOf
            }
        }
        return 0
    }
}
