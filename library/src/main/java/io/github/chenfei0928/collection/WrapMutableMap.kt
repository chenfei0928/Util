package io.github.chenfei0928.collection

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.collection.ArrayMap
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Function

/**
 * @author chenf()
 * @date 2024-12-31 17:22
 */
@Suppress("TooManyFunctions")
open class WrapMutableMap<WK, K, WV, V>(
    internal val map: MutableMap<K, V> = ArrayMap(),
    private val keyConvertor: WrapMutableMapConvertor.Key<WK, K>,
    private val valueConvertor: WrapMutableMapConvertor.Value<WV, V>,
) : MutableMap<WK, WV>,
    WrapMutableMapConvertor.Key<WK, K> by keyConvertor,
    WrapMutableMapConvertor.Value<WV, V> by valueConvertor {

    init {
        if (keyConvertor is WrapMutableMapConvertor.Hostable<*, *, *, *>) {
            @Suppress("UNCHECKED_CAST")
            (keyConvertor as WrapMutableMapConvertor.Hostable<WK, K, WV, V>).hostMap = this
        }
        if (valueConvertor is WrapMutableMapConvertor.Hostable<*, *, *, *>) {
            @Suppress("UNCHECKED_CAST")
            (valueConvertor as WrapMutableMapConvertor.Hostable<WK, K, WV, V>).hostMap = this
        }
    }

    private fun onAnyOperation() {
        if (keyConvertor is WrapMutableMapConvertor.Hostable<*, *, *, *>) {
            keyConvertor.onAnyOperation()
        }
        if (valueConvertor is WrapMutableMapConvertor.Hostable<*, *, *, *>) {
            valueConvertor.onAnyOperation()
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<WK, WV>> by lazy {
        Entries(this, map.entries, keyConvertor, valueConvertor)
    }
    override val keys: MutableSet<WK> by lazy {
        Keys(this, map.keys, keyConvertor)
    }
    override val values: MutableCollection<WV> by lazy {
        Values(this, map.values, valueConvertor)
    }

    private class Entries<WK, K, WV, V>(
        private val hostMap: WrapMutableMap<WK, K, WV, V>,
        private val entries: MutableSet<MutableMap.MutableEntry<K, V>>,
        private val keyConvertor: WrapMutableMapConvertor.Key<WK, K>,
        private val valueConvertor: WrapMutableMapConvertor.Value<WV, V>,
    ) : AbstractMutableSet<MutableMap.MutableEntry<WK, WV>>(),
        WrapMutableMapConvertor.Key<WK, K> by keyConvertor,
        WrapMutableMapConvertor.Value<WV, V> by valueConvertor {
        override fun add(element: MutableMap.MutableEntry<WK, WV>): Boolean {
            return entries.add(object : MutableMap.MutableEntry<K, V> {
                override fun setValue(newValue: V): V {
                    return element.setValue(newValue!!.toWV())!!.toV()
                }

                override val key: K
                    get() = element.key!!.toK()
                override val value: V
                    get() = element.value!!.toV()
            })
        }

        override fun clear() {
            hostMap.onAnyOperation()
            entries.clear()
        }

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<WK, WV>> {
            return object : MutableIterator<MutableMap.MutableEntry<WK, WV>> {
                private val iterator = entries.iterator()
                override fun remove() {
                    hostMap.onAnyOperation()
                    iterator.remove()
                }

                override fun hasNext(): Boolean {
                    hostMap.onAnyOperation()
                    return iterator.hasNext()
                }

                override fun next(): MutableMap.MutableEntry<WK, WV> {
                    return object : MutableMap.MutableEntry<WK, WV> {
                        private val next = iterator.next()
                        override val key: WK
                            get() = next.key!!.toWK()
                        override val value: WV
                            get() = next.value!!.toWV()

                        override fun setValue(newValue: WV): WV {
                            return next.setValue(newValue!!.toV())!!.toWV()
                        }
                    }
                }
            }
        }

        override val size: Int
            get() {
                hostMap.onAnyOperation()
                return entries.size
            }

        override fun isEmpty(): Boolean {
            hostMap.onAnyOperation()
            return entries.isEmpty()
        }
    }

    private class Keys<WK, K>(
        private val hostMap: WrapMutableMap<WK, K, *, *>,
        private val keys: MutableSet<K>,
        private val keyConvertor: WrapMutableMapConvertor.Key<WK, K>,
    ) : AbstractMutableSet<WK>(),
        WrapMutableMapConvertor.Key<WK, K> by keyConvertor {
        override fun add(element: WK): Boolean {
            hostMap.onAnyOperation()
            return keys.add(element!!.toK())
        }

        override fun clear() {
            hostMap.onAnyOperation()
            keys.clear()
        }

        override fun iterator(): MutableIterator<WK> {
            return object : MutableIterator<WK> {
                private val iterator = keys.iterator()
                override fun remove() {
                    hostMap.onAnyOperation()
                    iterator.remove()
                }

                override fun hasNext(): Boolean {
                    hostMap.onAnyOperation()
                    return iterator.hasNext()
                }

                override fun next(): WK {
                    hostMap.onAnyOperation()
                    return iterator.next()!!.toWK()
                }
            }
        }

        override val size: Int
            get() {
                hostMap.onAnyOperation()
                return keys.size
            }

        override fun isEmpty(): Boolean {
            hostMap.onAnyOperation()
            return keys.isEmpty()
        }
    }

    private class Values<WV, V>(
        private val hostMap: WrapMutableMap<*, *, WV, V>,
        private val values: MutableCollection<V>,
        private val valueConvertor: WrapMutableMapConvertor.Value<WV, V>,
    ) : AbstractMutableCollection<WV>(),
        WrapMutableMapConvertor.Value<WV, V> by valueConvertor {
        override fun add(element: WV): Boolean {
            hostMap.onAnyOperation()
            return values.add(element!!.toV())
        }

        override fun iterator(): MutableIterator<WV> {
            return object : MutableIterator<WV> {
                private val iterator = values.iterator()
                override fun remove() {
                    hostMap.onAnyOperation()
                    iterator.remove()
                }

                override fun hasNext(): Boolean {
                    hostMap.onAnyOperation()
                    return iterator.hasNext()
                }

                override fun next(): WV {
                    return iterator.next()!!.toWV()
                }
            }
        }

        override val size: Int
            get() = values.size
    }

    override fun clear() {
        map.clear()
        onAnyOperation()
    }

    override fun put(key: WK, value: WV): WV? {
        onAnyOperation()
        return map.put(key!!.toK(), value!!.toV())?.toWV()
    }

    override fun putAll(from: Map<out WK, WV>) {
        onAnyOperation()
        map.putAll(from.mapKeys { it.key!!.toK() }.mapValues { it.value!!.toV() })
    }

    override fun remove(key: WK): WV? {
        onAnyOperation()
        return map.remove(key!!.toK())?.toWV()
    }

    override fun remove(key: WK, value: WV): Boolean {
        onAnyOperation()
        return map.remove(key!!.toK(), value!!.toV())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun replaceAll(function: BiFunction<in WK, in WV, out WV>) {
        onAnyOperation()
        map.replaceAll { key, value ->
            function.apply(key!!.toWK(), value!!.toWV()).toV()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun putIfAbsent(key: WK, value: WV): WV? {
        onAnyOperation()
        return map.putIfAbsent(key!!.toK(), value!!.toV())?.toWV()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun replace(key: WK, oldValue: WV, newValue: WV): Boolean {
        onAnyOperation()
        return map.replace(key!!.toK(), oldValue!!.toV(), newValue!!.toV())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun replace(key: WK, value: WV): WV? {
        onAnyOperation()
        return map.replace(key!!.toK(), value!!.toV())?.toWV()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun computeIfAbsent(
        key: WK,
        mappingFunction: Function<in WK, out WV>
    ): WV {
        onAnyOperation()
        return map.computeIfAbsent(key!!.toK()) {
            mappingFunction.apply(it!!.toWK()).toV()
        }!!.toWV()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun computeIfPresent(
        key: WK,
        remappingFunction: BiFunction<in WK, in WV & Any, out WV?>
    ): WV? {
        onAnyOperation()
        return map.computeIfPresent(key!!.toK()) { key, value ->
            remappingFunction.apply(key!!.toWK(), value.toWV()!!).toV()
        }?.toWV()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun compute(
        key: WK,
        remappingFunction: BiFunction<in WK, in WV?, out WV?>
    ): WV? {
        onAnyOperation()
        return map.compute(key!!.toK()) { key, value ->
            remappingFunction.apply(key!!.toWK(), value?.toWV()).toV()
        }?.toWV()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun merge(
        key: WK,
        value: WV & Any,
        remappingFunction: BiFunction<in WV & Any, in WV & Any, out WV?>
    ): WV? {
        onAnyOperation()
        return map.merge(key!!.toK(), value.toV()!!) { oldValue, value ->
            remappingFunction.apply(oldValue.toWV()!!, value.toWV()!!).toV()
        }?.toWV()
    }

    override val size: Int
        get() {
            onAnyOperation()
            return map.size
        }

    override fun isEmpty(): Boolean {
        onAnyOperation()
        return map.isEmpty()
    }

    override fun containsKey(key: WK): Boolean {
        onAnyOperation()
        return map.containsKey(key!!.toK())
    }

    override fun containsValue(value: WV): Boolean {
        onAnyOperation()
        return map.containsValue(value!!.toV())
    }

    override fun get(key: WK): WV? {
        onAnyOperation()
        return map[key!!.toK()]?.toWV()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getOrDefault(key: WK, defaultValue: WV): WV {
        onAnyOperation()
        return map.getOrDefault(key!!.toK(), defaultValue!!.toV())!!.toWV() ?: defaultValue
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun forEach(action: BiConsumer<in WK, in WV>) {
        onAnyOperation()
        map.forEach {
            action.accept(it.key!!.toWK(), it.value!!.toWV())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == this)
            return true

        if (other !is Map<*, *>)
            return false
        if (other.size != size)
            return false

        try {
            entries.forEach { e ->
                val key = e.key
                val value = e.value
                if (value == null) {
                    if (!(other[key] == null && other.containsKey(key)))
                        return false
                } else {
                    if (value != other[key])
                        return false
                }
            }
        } catch (_: ClassCastException) {
            return false
        } catch (_: NullPointerException) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var h = 0
        entries.forEach {
            h += it.hashCode()
        }
        return h
    }

    override fun toString(): String {
        return if (isEmpty()) {
            "{}"
        } else buildString {
            append('{')
            val size = size
            entries.forEachIndexed { index, (key, value) ->
                append(if (key == this@WrapMutableMap) "(this Map)" else key)
                append("=")
                append(if (value == this@WrapMutableMap) "(this Map)" else value)
                append(if (index == size) "}" else ", ")
            }
        }
    }
}
