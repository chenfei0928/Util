/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-15 16:13
 */
package com.drakeet.multitype

fun MultiTypeAdapter.notifyItemChanged(item: Any) {
    val indexOf = items.indexOf(item)
    if (indexOf >= 0) {
        notifyItemChanged(indexOf)
    }
}
