package io.github.chenfei0928.collection

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.collection.ArrayMap
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Function
import kotlin.collections.MutableMap

/**
 * @author chenf()
 * @date 2024-11-22 15:56
 */
class SystemIdentityMutableMap<K, V>(
    private val map: MutableMap<HashCodeWrapper<K>, V> = ArrayMap()
) : MutableMap<K, V> {

    override fun clear() {
        map.clear()
    }

    override fun put(key: K, value: V): V? {
        return map.put(HashCodeWrapper(key), value)
    }

    override fun putAll(from: Map<out K, V>) {
        map.putAll(from.mapKeys { HashCodeWrapper(it.key) })
    }

    override fun remove(key: K): V? {
        return map.remove(HashCodeWrapper(key))
    }

    override fun remove(key: K, value: V): Boolean {
        return map.remove(HashCodeWrapper(key), value)
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> by lazy {
        Entries(map.entries)
    }
    override val keys: MutableSet<K> by lazy {
        Keys(map.keys)
    }
    override val values: MutableCollection<V>
        get() = map.values

    private class Entries<K, V>(
        entries: MutableSet<MutableMap.MutableEntry<HashCodeWrapper<K>, V>>
    ) : WrapperMutableSet<MutableMap.MutableEntry<HashCodeWrapper<K>, V>, MutableMap.MutableEntry<K, V>>(
        entries
    ) {
        override fun MutableMap.MutableEntry<K, V>.toT(): MutableMap.MutableEntry<HashCodeWrapper<K>, V> {
            return object : MutableEntry<K, HashCodeWrapper<K>, V>(this) {
                override val key: HashCodeWrapper<K> = HashCodeWrapper(entry.key)
            }
        }

        override fun MutableMap.MutableEntry<HashCodeWrapper<K>, V>.toR(): MutableMap.MutableEntry<K, V> {
            return object : MutableEntry<HashCodeWrapper<K>, K, V>(this) {
                override val key: K = entry.key.ref
            }
        }
    }

    private abstract class MutableEntry<W, K, V>(
        val entry: MutableMap.MutableEntry<W, V>,
    ) : MutableMap.MutableEntry<K, V> {
        override val value: V = entry.value

        override fun setValue(newValue: V): V {
            return entry.setValue(newValue)
        }
    }

    private class Keys<K>(
        keys: MutableSet<HashCodeWrapper<K>>
    ) : WrapperMutableSet<HashCodeWrapper<K>, K>(keys) {
        override fun K.toT(): HashCodeWrapper<K> {
            return HashCodeWrapper(this)
        }

        override fun HashCodeWrapper<K>.toR(): K {
            return ref
        }
    }

    private abstract class WrapperMutableSet<T, R>(
        val set: MutableSet<T>
    ) : AbstractMutableSet<R>() {
        abstract fun T.toR(): R
        abstract fun R.toT(): T

        override fun add(element: R): Boolean {
            return set.add(element.toT())
        }

        override fun clear() {
            set.clear()
        }

        override fun iterator(): MutableIterator<R> {
            return object : WrapperIterator<T, R>(set.iterator()) {
                override fun next(): R {
                    return iterator.next().toR()
                }
            }
        }

        override val size: Int
            get() = set.size

        override fun isEmpty(): Boolean {
            return set.isEmpty()
        }
    }

    private abstract class WrapperIterator<T, R>(
        val iterator: MutableIterator<T>
    ) : MutableIterator<R> {
        override fun remove() {
            iterator.remove()
        }

        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun replaceAll(function: BiFunction<in K, in V, out V>) {
        map.replaceAll { key, value ->
            return@replaceAll function.apply(key.ref, value)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun putIfAbsent(key: K, value: V): V? {
        return map.putIfAbsent(HashCodeWrapper(key), value)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun replace(key: K, oldValue: V, newValue: V): Boolean {
        return map.replace(HashCodeWrapper(key), oldValue, newValue)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun replace(key: K, value: V): V? {
        return map.replace(HashCodeWrapper(key), value)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun computeIfAbsent(
        key: K,
        mappingFunction: Function<in K, out V>
    ): V {
        return map.computeIfAbsent(HashCodeWrapper(key)) {
            mappingFunction.apply(it.ref)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun computeIfPresent(
        key: K,
        remappingFunction: BiFunction<in K, in V & Any, out V?>
    ): V? {
        return map.computeIfPresent(HashCodeWrapper(key)) { key, value ->
            remappingFunction.apply(key.ref, value)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun compute(
        key: K,
        remappingFunction: BiFunction<in K, in V?, out V?>
    ): V? {
        return map.compute(HashCodeWrapper(key)) { key, value ->
            remappingFunction.apply(key.ref, value)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun merge(
        key: K,
        value: V & Any,
        remappingFunction: BiFunction<in V & Any, in V & Any, out V?>
    ): V? {
        return map.merge(HashCodeWrapper(key), value, remappingFunction)
    }

    override val size: Int
        get() = map.size

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override fun containsKey(key: K): Boolean {
        return map.containsKey(HashCodeWrapper(key))
    }

    override fun containsValue(value: V): Boolean {
        return map.containsValue(value)
    }

    override fun get(key: K): V? {
        return map[HashCodeWrapper(key)]
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getOrDefault(key: K, defaultValue: V): V {
        return map.getOrDefault(HashCodeWrapper(key), defaultValue)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun forEach(action: BiConsumer<in K, in V>) {
        map.forEach {
            action.accept(it.key.ref, it.value)
        }
    }

    override fun equals(other: Any?): Boolean {
        return map == other
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    override fun toString(): String {
        return map.toString()
    }

    class HashCodeWrapper<T>(
        val ref: T
    ) {
        override fun equals(other: Any?): Boolean {
            return ref == other
        }

        override fun hashCode(): Int {
            return System.identityHashCode(ref)
        }

        override fun toString(): String {
            return ref.toString()
        }
    }
}
