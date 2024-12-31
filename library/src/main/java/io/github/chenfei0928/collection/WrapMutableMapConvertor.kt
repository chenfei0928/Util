package io.github.chenfei0928.collection

/**
 * @author chenf()
 * @date 2024-12-31 18:09
 */
interface WrapMutableMapConvertor {

    interface Key<WK, K> {
        fun <WKA : WK & Any> WKA.toK(): K
        fun <KA : K & Any> KA.toWK(): WK

        class NoTodo<K> : Key<K, K> {
            override fun <WKA : K & Any> WKA.toK(): K {
                return this
            }

            override fun <KA : K & Any> KA.toWK(): K {
                return this
            }
        }

        class SystemHashCode<K> : Key<K, SystemHashCode.HashCodeWrapper<K>> {
            override fun <WKA : K & Any> WKA.toK(): HashCodeWrapper<K> {
                return HashCodeWrapper(this)
            }

            override fun <KA : HashCodeWrapper<K>> KA.toWK(): K {
                return ref
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
    }

    interface Value<WV, V> {
        fun <WVA : WV & Any> WVA.toV(): V
        fun <VA : V & Any> VA.toWV(): WV

        class NoTodo<V> : Value<V, V> {
            override fun <WVA : V & Any> WVA.toV(): V {
                return this
            }

            override fun <VA : V & Any> VA.toWV(): V {
                return this
            }
        }

        class WeakReference<V> : Value<V?, java.lang.ref.WeakReference<V>> {
            override fun <WVA : V & Any> WVA.toV(): java.lang.ref.WeakReference<V> {
                return java.lang.ref.WeakReference(this)
            }

            override fun <VA : java.lang.ref.WeakReference<V>> VA.toWV(): V? {
                return get()
            }
        }
    }
}
