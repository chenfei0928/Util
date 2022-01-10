package io.github.chenfei0928.collection

import java.util.*
import kotlin.collections.ArrayList

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the collection.
 */
inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum: Long = 0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun <T> Iterator<T>.collect(): List<T> {
    val output = LinkedList<T>()
    forEach { output.add(it) }
    return output
}

/**
 * 与门，取交集，同属于A和B
 */
infix fun <T> Collection<T>.and(that: Iterable<T>) = this.filter { it in that }

fun <T> List<T>.asArrayList(): ArrayList<T> = this as? ArrayList ?: ArrayList(this)

/**
 * 删除一个范围内的数据
 */
fun <T> MutableList<T>.removeRange(range: IntProgression) {
    // 保证其从大到小排列
    val downToProcession = if (range.first < range.last) {
        range.reversed()
    } else {
        range
    }
    // 倒序删除项目，此处不使用subList.clear，其内部使用迭代器实现，可能会更为消耗性能
    downToProcession.forEach {
        removeAt(it)
    }
}

inline fun <K, V> MutableMap<K, V?>.getContainOrPut(key: K, defaultValue: () -> V?): V? {
    return if (containsKey(key)) {
        get(key)
    } else {
        val answer = defaultValue()
        put(key, answer)
        answer
    }
}
