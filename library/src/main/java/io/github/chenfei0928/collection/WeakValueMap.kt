package io.github.chenfei0928.collection

import androidx.collection.ArrayMap
import java.lang.ref.WeakReference

/**
 * @author chenf()
 * @date 2024-12-31 16:45
 */
class WeakValueMap<K, V>(
    map: MutableMap<K, WeakReference<V>> = ArrayMap()
) : WrapMutableMap<K, K, V, WeakReference<V>>(
    map, WrapMutableMapConvertor.Key.NoTodo(), WrapMutableMapConvertor.Value.WeakRef()
)
