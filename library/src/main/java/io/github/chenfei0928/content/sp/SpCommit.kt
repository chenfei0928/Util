package io.github.chenfei0928.content.sp

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-13 17:04
 */
interface SpCommit {
    fun getSpAll(): Map<String, *>
    fun clear()
    fun commit(): Boolean
    fun apply()
}
