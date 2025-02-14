package io.github.chenfei0928.content.sp.saver

import kotlin.reflect.KProperty

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-13 17:04
 */
interface SpCommit {
    /**
     * 获取指定的字段在实际本地存储中是否存在（抑或获取时候会返回默认值）
     */
    operator fun contains(key: String): Boolean

    /**
     * 获取指定的字段在实际本地存储中是否存在（抑或获取时候会返回默认值）
     */
    operator fun contains(property: KProperty<*>): Boolean
    fun getSpAll(): Map<String, *>
    fun remove(key: String)
    fun remove(property: KProperty<*>)
    fun clear()
    fun commit(): Boolean
    fun apply()
}
