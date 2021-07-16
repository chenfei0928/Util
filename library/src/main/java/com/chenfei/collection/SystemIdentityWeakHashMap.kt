package com.chenfei.collection

import android.os.Build
import java.util.*
import java.util.SystemIdentityWeakHashMap

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-29 17:20
 */
class SystemIdentityWeakHashMap<K, V>(
    private val map: MutableMap<K, V> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SystemIdentityWeakHashMapN<K, V>()
    } else {
        SystemIdentityWeakHashMap<K, V>()
    }
) : MutableMap<K, V> by map
