package io.github.chenfei0928.widget.recyclerview

import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import io.github.chenfei0928.view.getTagOrPut
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * 字段委托类，通过viewTag来实现为viewHolder扩展字段
 */
class ViewHolderTagValDelegate<VH : RecyclerView.ViewHolder, R>(
    @all:IdRes private val id: Int,
    private val creator: (VH) -> R
) : ReadOnlyProperty<VH, R> {

    override fun getValue(thisRef: VH, property: KProperty<*>): R {
        return thisRef.itemView.getTagOrPut(id) {
            creator(thisRef)
        }
    }
}
