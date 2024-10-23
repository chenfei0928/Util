package androidx.viewpager2.adapter

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import io.github.chenfei0928.os.getParcelableCompat
import io.github.chenfei0928.widget.recyclerview.adapter.IMultiTypeAdapterStringer

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2023-03-01 17:32
 */
open class FragmentStateMultiTypeAdapter
@JvmOverloads
constructor(
    val mFragmentManager: FragmentManager,
    val mLifecycle: Lifecycle,
    items: List<Any> = emptyList(),
    initialCapacity: Int = 0,
    types: AttachedTypes = FragmentStateMultiTypes(initialCapacity),
) : IMultiTypeAdapterStringer.IMultiTypeAdapter(items, initialCapacity, types), StatefulAdapter {

    //<editor-fold desc="构造器" defaultstatus="collapsed">
    @JvmOverloads
    constructor(
        fragmentActivity: FragmentActivity,
        items: List<Any> = emptyList(),
        initialCapacity: Int = 0,
        types: AttachedTypes = FragmentStateMultiTypes(initialCapacity),
    ) : this(
        fragmentActivity.supportFragmentManager,
        fragmentActivity.lifecycle,
        items,
        initialCapacity,
        types,
    )

    @JvmOverloads
    constructor(
        fragmentActivity: Fragment,
        items: List<Any> = emptyList(),
        initialCapacity: Int = 0,
        types: AttachedTypes = FragmentStateMultiTypes(initialCapacity),
    ) : this(
        fragmentActivity.childFragmentManager,
        fragmentActivity.lifecycle,
        items,
        initialCapacity,
        types,
    )

    init {
        super.setHasStableIds(true)
        types.adapter = this
    }
    //</editor-fold>

    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        (types as AttachedTypes).onAttachedToRecyclerView(recyclerView)
    }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        (types as AttachedTypes).onDetachedFromRecyclerView(recyclerView)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>
    ) {
        val itemViewDelegate = types.getType<Any>(holder.itemViewType).delegate
        if (itemViewDelegate is FragmentBinder<Any>) {
            val item = items[position]
            itemViewDelegate.onBindViewHolder(
                holder as MultiTypeFragmentViewHolder<Any>, position, item, payloads
            )
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    /**
     * Default implementation works for collections that don't add, move, remove items.
     *
     *
     * TODO(b/122670460): add lint rule
     * When overriding, also override [.containsItem].
     *
     *
     * If the item is not a part of the collection, return [RecyclerView.NO_ID].
     *
     * @param position Adapter position
     * @return stable item id [RecyclerView.Adapter.hasStableIds]
     */
    override fun getItemId(position: Int): Long {
        val itemViewDelegate = types.getType<Any>(getItemViewType(position)).delegate
        return if (itemViewDelegate is FragmentBinder<Any>) {
            val item = items[position]
            itemViewDelegate.getItemId(position, item)
        } else {
            super.getItemId(position)
        }
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        throw UnsupportedOperationException(
            "Stable Ids are required for the adapter to function properly, and the adapter "
                    + "takes care of setting the flag."
        )
    }

    override fun saveState(): Parcelable {
        val bundle = Bundle()
        for (i in 0 until types.size) {
            val type = types.getType<Any>(i).delegate
            if (type is FragmentBinder<*>) {
                bundle.putParcelable(type.javaClass.name, type.saveState())
            }
        }
        return bundle
    }

    override fun restoreState(savedState: Parcelable) {
        savedState as Bundle
        for (i in 0 until types.size) {
            val type = types.getType<Any>(i).delegate
            if (type is FragmentBinder<*>) {
                savedState.getParcelableCompat<Parcelable>(
                    type.javaClass.name
                )?.let {
                    type.restoreState(it)
                }
            }
        }
    }

    companion object {
        private const val TAG = "FragmentStateMultiTypeA"
    }
}
