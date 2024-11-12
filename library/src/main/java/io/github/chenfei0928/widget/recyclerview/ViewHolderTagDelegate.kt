package io.github.chenfei0928.widget.recyclerview

import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import io.github.chenfei0928.view.ViewTagDelegate
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 字段委托类，通过viewTag来实现为viewHolder扩展字段
 */
class ViewHolderTagDelegate<R>(
    @IdRes id: Int
) : ReadWriteProperty<RecyclerView.ViewHolder, R?> {
    private var View.delegateImpl: R? by ViewTagDelegate(id)

    override fun getValue(thisRef: RecyclerView.ViewHolder, property: KProperty<*>): R? {
        return thisRef.itemView.delegateImpl
    }

    override fun setValue(thisRef: RecyclerView.ViewHolder, property: KProperty<*>, value: R?) {
        thisRef.itemView.delegateImpl = value
    }
}
