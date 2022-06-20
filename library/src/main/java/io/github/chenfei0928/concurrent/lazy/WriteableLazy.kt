package io.github.chenfei0928.concurrent.lazy

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author chenfei()
 * @date 2022-06-20 13:33
 */
class WriteableLazy<in T, V>(
    lazy: Lazy<V>, lock: Any? = null
) : ReadWriteProperty<T, V>, Lazy<V> {
    private var lazy: Lazy<V>? = lazy

    // final field is required to enable safe publication of constructed instance
    private val lock = lock ?: this
    private var _value: Any? = UNINITIALIZED_VALUE

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        return value
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        lazy = null
        this._value = value
    }

    override val value: V
        get() {
            val _v1 = _value
            if (_v1 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST")
                return _v1 as V
            }

            return synchronized(lock) {
                val _v2 = _value
                if (_v2 !== UNINITIALIZED_VALUE) {
                    @Suppress("UNCHECKED_CAST") (_v2 as V)
                } else {
                    val typedValue = lazy!!.value
                    _value = typedValue
                    lazy = null
                    typedValue
                }
            }
        }

    override fun isInitialized(): Boolean =
        _value !is UNINITIALIZED_VALUE || lazy.let { it == null || it.isInitialized() }
}

fun <T, V> Lazy<V>.toWriteable(): ReadWriteProperty<T, V> =
    WriteableLazy(this)
