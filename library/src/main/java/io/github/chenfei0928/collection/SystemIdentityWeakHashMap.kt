package io.github.chenfei0928.collection

import android.os.Build

/**
 * 使用 {@link System#identityHashCode(Object)} 获取 hash 的{@link WeakHashMap}实现
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-29 17:20
 */
class SystemIdentityWeakHashMap<K, V>(
    private val map: MutableMap<K, V> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SystemIdentityWeakHashMapN<K, V>()
    } else {
        BaseSystemIdentityWeakHashMap<K, V>()
    }
) : MutableMap<K, V> by map
