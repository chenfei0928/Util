package java.util

import android.os.Build

/**
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
