package io.github.chenfei0928.collection

import androidx.collection.ArrayMap
import kotlin.collections.MutableMap

/**
 * @author chenf()
 * @date 2024-11-22 15:56
 */
class SystemIdentityMutableMap<K, V>(
    map: MutableMap<WrapMutableMapConvertor.Key.SystemHashCode.HashCodeWrapper<K>, V> = ArrayMap(),
) : WrapMutableMap<K, WrapMutableMapConvertor.Key.SystemHashCode.HashCodeWrapper<K>, V, V>(
    map, WrapMutableMapConvertor.Key.SystemHashCode(), WrapMutableMapConvertor.Value.NoTodo()
)
