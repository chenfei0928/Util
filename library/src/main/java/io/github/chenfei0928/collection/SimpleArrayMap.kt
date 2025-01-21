/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-21 13:30
 */
package io.github.chenfei0928.collection

import androidx.collection.SimpleArrayMap

/**
 * 将[Map]转换为[SimpleArrayMap]，[SimpleArrayMap]的实现性能更高
 */
fun <K, V> Map<K, V>.asSimpleArrayMap(): SimpleArrayMap<K, V> = if (this is SimpleArrayMap<*, *>) {
    @Suppress("UNCHECKED_CAST")
    this as SimpleArrayMap<K, V>
} else {
    SimpleArrayMap<K, V>(size).also {
        this.forEach { (k, v) ->
            it.put(k, v)
        }
    }
}

inline operator fun <K, V> SimpleArrayMap<K, V>.set(key: K, value: V): V? = put(key, value)

/**
 * 使用专属优化的根据[SimpleArrayMap.size]大小进行遍历，减少对象创建，同时该实现性能更好
 */
inline fun <K, V> SimpleArrayMap<K, V>.forEach(block: (K, V) -> Unit) {
    for (i in 0 until size()) {
        block(keyAt(i), valueAt(i))
    }
}
