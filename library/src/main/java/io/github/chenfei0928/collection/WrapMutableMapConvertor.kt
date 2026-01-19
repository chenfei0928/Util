package io.github.chenfei0928.collection

import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference

/**
 * @author chenf()
 * @date 2024-12-31 18:09
 */
interface WrapMutableMapConvertor {

    interface Hostable<WK, K, WV, V> {
        var hostMap: WrapMutableMap<WK, K, WV, V>

        fun onAnyOperation()
    }

    interface Key<WK, K> {
        fun <WKA : WK & Any> WKA.toK(): K
        fun <KA : K & Any> KA.toWK(): WK

        class NoTodo<K> : Key<K, K> {
            override fun <WKA : K & Any> WKA.toK(): K = this
            override fun <KA : K & Any> KA.toWK(): K = this
        }

        class SystemHashCode<K> : Key<K, SystemHashCode.HashCodeWrapper<K>> {
            override fun <WKA : K & Any> WKA.toK(): HashCodeWrapper<K> = HashCodeWrapper(this)
            override fun <KA : HashCodeWrapper<K>> KA.toWK(): K = ref

            class HashCodeWrapper<T>(val ref: T) {
                override fun equals(other: Any?): Boolean = ref === other
                override fun hashCode(): Int = System.identityHashCode(ref)
                override fun toString(): String = ref.toString()
            }
        }
    }

    interface Value<WV, V> {
        fun <WVA : WV & Any> WVA.toV(): V
        fun <VA : V & Any> VA.toWV(): WV

        class NoTodo<V> : Value<V, V> {
            override fun <WVA : V & Any> WVA.toV(): V = this
            override fun <VA : V & Any> VA.toWV(): V = this
        }

        class WeakRef<V> : Value<V, WeakReference<V>>, Hostable<Any, Any, V, WeakReference<V>> {
            private val queue = ReferenceQueue<V>()
            override lateinit var hostMap: WrapMutableMap<Any, Any, V, WeakReference<V>>

            override fun <WVA : V & Any> WVA.toV(): WeakReference<V> = WeakReference(this, queue)
            override fun <VA : WeakReference<V>> VA.toWV(): V = get()!!
            private var protectFromCheckedChange: Boolean = false
            override fun onAnyOperation() {
                if (protectFromCheckedChange) {
                    return
                }
                protectFromCheckedChange = true
                val x = queue.poll()
                while (x != null) {
                    hostMap.map.entries.removeAll { it.value == x }
                }
                protectFromCheckedChange = false
            }
        }
    }
}
