package androidx.viewpager2.adapter

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.collection.ArraySet
import androidx.collection.LongSparseArray
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.drakeet.multitype.ItemViewDelegate
import com.drakeet.multitype.MultiTypeAdapter
import io.github.chenfei0928.os.getParcelableCompat

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2023-03-01 18:09
 */
abstract class FragmentBinder<T> : ItemViewDelegate<T, MultiTypeFragmentViewHolder<T>>(),
    StatefulAdapter {

    //<editor-fold desc="不使用的父类方法和Adapter接口访问" defaultstatus="collapsed">
    val fragmentAdapter: FragmentStateMultiTypeAdapter
        get() = super.adapter as FragmentStateMultiTypeAdapter
    val mFragmentManager: FragmentManager
        get() = fragmentAdapter.mFragmentManager
    val mLifecycle: Lifecycle
        get() = fragmentAdapter.mLifecycle

    final override fun getItemId(item: T): Long {
        throw IllegalAccessException("不支持的方法")
    }

    final override fun onBindViewHolder(
        holder: MultiTypeFragmentViewHolder<T>, item: T, payloads: List<Any>
    ) {
        throw IllegalAccessException("不支持的方法")
    }

    final override fun onBindViewHolder(holder: MultiTypeFragmentViewHolder<T>, item: T) {
        throw IllegalAccessException("不支持的方法")
    }
    //</editor-fold>

    // Fragment bookkeeping
    // to avoid creation of a synthetic accessor
    internal val mFragments = LongSparseArray<Fragment>()
    private val mSavedStates = LongSparseArray<Fragment.SavedState>()
    private val mItemIdToViewHolder = LongSparseArray<Int>()

    private var mFragmentMaxLifecycleEnforcer: FragmentMaxLifecycleEnforcer? = null

    // Fragment GC
    // to avoid creation of a synthetic accessor
    internal var mIsInGracePeriod = false
    private var mHasStaleFragments = false

    @CallSuper
    open fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        require(mFragmentMaxLifecycleEnforcer == null)
        mFragmentMaxLifecycleEnforcer = FragmentMaxLifecycleEnforcer()
        mFragmentMaxLifecycleEnforcer!!.register(recyclerView)
    }

    @CallSuper
    open fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        mFragmentMaxLifecycleEnforcer!!.unregister(recyclerView)
        mFragmentMaxLifecycleEnforcer = null
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup
    ): MultiTypeFragmentViewHolder<T> {
        return MultiTypeFragmentViewHolder.create(parent)
    }

    open fun onBindViewHolder(
        holder: MultiTypeFragmentViewHolder<T>, position: Int, item: T, payloads: List<Any>
    ) {
        val itemId = holder.itemId
        val viewHolderId = holder.container.id
        val boundItemId = itemForViewHolder(viewHolderId) // item currently bound to the VH
        if (boundItemId != null && boundItemId != itemId) {
            removeFragment(boundItemId)
            mItemIdToViewHolder.remove(boundItemId)
        }
        mItemIdToViewHolder.put(itemId, viewHolderId) // this might overwrite an existing entry
        ensureFragment(holder, position, item)
        /** Special case when [RecyclerView] decides to keep the [container]
         * attached to the window, but not to the view hierarchy (i.e. parent is null)  */
        val container = holder.container
        if (container.isAttachedToWindow) {
            check(container.parent == null) { "Design assumption violated." }
            container.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View, left: Int, top: Int, right: Int, bottom: Int,
                    oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
                ) {
                    if (container.parent != null) {
                        container.removeOnLayoutChangeListener(this)
                        placeFragmentInViewHolder(holder)
                    }
                }
            })
        }
        gcFragments()
    }

    /**
     * Provide a new Fragment associated with the specified position.
     *
     *
     * The adapter will be responsible for the Fragment lifecycle:
     *
     *  * The Fragment will be used to display an item.
     *  * The Fragment will be destroyed when it gets too far from the viewport, and its state
     * will be saved. When the item is close to the viewport again, a new Fragment will be
     * requested, and a previously saved state will be used to initialize it.
     *
     * @see ViewPager2.setOffscreenPageLimit
     */
    protected abstract fun createFragment(
        holder: MultiTypeFragmentViewHolder<T>,
        position: Int,
        item: T
    ): Fragment

    // to avoid creation of a synthetic accessor
    internal fun gcFragments() {
        if (!mHasStaleFragments || shouldDelayFragmentTransactions()) {
            return
        }

        // Remove Fragments for items that are no longer part of the data-set
        val toRemove: MutableSet<Long> = ArraySet()
        for (ix in 0 until mFragments.size()) {
            val itemId = mFragments.keyAt(ix)
            if (!containsItem(itemId)) {
                toRemove.add(itemId)
                mItemIdToViewHolder.remove(itemId) // in case they're still bound
            }
        }

        // Remove Fragments that are not bound anywhere -- pending a grace period
        if (!mIsInGracePeriod) {
            mHasStaleFragments = false // we've executed all GC checks
            for (ix in 0 until mFragments.size()) {
                val itemId = mFragments.keyAt(ix)
                if (!isFragmentViewBound(itemId)) {
                    toRemove.add(itemId)
                }
            }
        }
        for (itemId in toRemove) {
            removeFragment(itemId)
        }
    }

    private fun isFragmentViewBound(itemId: Long): Boolean {
        if (mItemIdToViewHolder.containsKey(itemId)) {
            return true
        }
        val fragment = mFragments[itemId] ?: return false
        val view = fragment.view ?: return false
        return view.parent != null
    }

    private fun itemForViewHolder(viewHolderId: Int): Long? {
        var boundItemId: Long? = null
        for (ix in 0 until mItemIdToViewHolder.size()) {
            if (mItemIdToViewHolder.valueAt(ix) == viewHolderId) {
                check(boundItemId == null) {
                    ("Design assumption violated: "
                            + "a ViewHolder can only be bound to one item at a time.")
                }
                boundItemId = mItemIdToViewHolder.keyAt(ix)
            }
        }
        return boundItemId
    }

    private fun ensureFragment(holder: MultiTypeFragmentViewHolder<T>, position: Int, item: T) {
        val itemId = getItemId(position, item)
        if (!mFragments.containsKey(itemId)) {
            // TODO(133419201): check if a Fragment provided here is a new Fragment
            val newFragment = createFragment(holder, position, item)
            newFragment.setInitialSavedState(mSavedStates[itemId])
            mFragments.put(itemId, newFragment)
        }
    }

    override fun onViewAttachedToWindow(holder: MultiTypeFragmentViewHolder<T>) {
        placeFragmentInViewHolder(holder)
        gcFragments()
    }

    /**
     * @param holder that has been bound to a Fragment in the [.onBindViewHolder] stage.
     */
    internal fun  // to avoid creation of a synthetic accessor
            placeFragmentInViewHolder(holder: MultiTypeFragmentViewHolder<T>) {
        val fragment = mFragments[holder.itemId]
            ?: throw java.lang.IllegalStateException("Design assumption violated.")
        val container = holder.container
        val view = fragment.view

        /*
        possible states:
        - fragment: { added, notAdded }
        - view: { created, notCreated }
        - view: { attached, notAttached }

        combinations:
        - { f:added, v:created, v:attached } -> check if attached to the right container
        - { f:added, v:created, v:notAttached} -> attach view to container
        - { f:added, v:notCreated, v:attached } -> impossible
        - { f:added, v:notCreated, v:notAttached} -> schedule callback for when created
        - { f:notAdded, v:created, v:attached } -> illegal state
        - { f:notAdded, v:created, v:notAttached } -> illegal state
        - { f:notAdded, v:notCreated, v:attached } -> impossible
        - { f:notAdded, v:notCreated, v:notAttached } -> add, create, attach
         */

        // { f:notAdded, v:created, v:attached } -> illegal state
        // { f:notAdded, v:created, v:notAttached } -> illegal state
        check(!(!fragment.isAdded && view != null)) { "Design assumption violated." }

        // { f:added, v:notCreated, v:notAttached} -> schedule callback for when created
        if (fragment.isAdded && view == null) {
            scheduleViewAttach(fragment, container)
            return
        }

        // { f:added, v:created, v:attached } -> check if attached to the right container
        if (fragment.isAdded && view!!.parent != null) {
            if (view.parent !== container) {
                addViewToContainer(view, container)
            }
            return
        }

        // { f:added, v:created, v:notAttached} -> attach view to container
        if (fragment.isAdded) {
            addViewToContainer(view!!, container)
            return
        }

        // { f:notAdded, v:notCreated, v:notAttached } -> add, create, attach
        if (!shouldDelayFragmentTransactions()) {
            scheduleViewAttach(fragment, container)
            mFragmentManager.beginTransaction()
                .add(fragment, "f" + holder.itemId)
                .setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                .commitNow()
            mFragmentMaxLifecycleEnforcer!!.updateFragmentMaxLifecycle(false)
        } else {
            if (mFragmentManager.isDestroyed) {
                return  // nothing we can do
            }
            mLifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(
                    source: LifecycleOwner,
                    event: Lifecycle.Event
                ) {
                    if (shouldDelayFragmentTransactions()) {
                        return
                    }
                    source.lifecycle.removeObserver(this)
                    if (holder.container.isAttachedToWindow) {
                        placeFragmentInViewHolder(holder)
                    }
                }
            })
        }
    }

    private fun scheduleViewAttach(fragment: Fragment, container: FrameLayout) {
        // After a config change, Fragments that were in FragmentManager will be recreated. Since
        // ViewHolder container ids are dynamically generated, we opted to manually handle
        // attaching Fragment views to containers. For consistency, we use the same mechanism for
        // all Fragment views.
        mFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                // TODO(b/141956012): Suppressed during upgrade to AGP 3.6.
                override fun onFragmentViewCreated(
                    fm: FragmentManager,
                    f: Fragment, v: View,
                    savedInstanceState: Bundle?
                ) {
                    if (f === fragment) {
                        fm.unregisterFragmentLifecycleCallbacks(this)
                        addViewToContainer(v, container)
                    }
                }
            }, false
        )
    }

    internal fun  // to avoid creation of a synthetic accessor
            addViewToContainer(v: View, container: FrameLayout) {
        check(container.childCount <= 1) { "Design assumption violated." }
        if (v.parent === container) {
            return
        }
        if (container.childCount > 0) {
            container.removeAllViews()
        }
        if (v.parent != null) {
            (v.parent as ViewGroup).removeView(v)
        }
        container.addView(v)
    }

    override fun onViewRecycled(holder: MultiTypeFragmentViewHolder<T>) {
        val viewHolderId = holder.container.id
        val boundItemId = itemForViewHolder(viewHolderId) // item currently bound to the VH
        if (boundItemId != null) {
            removeFragment(boundItemId)
            mItemIdToViewHolder.remove(boundItemId)
        }
    }

    override fun onFailedToRecycleView(holder: MultiTypeFragmentViewHolder<T>): Boolean {
        /*
         This happens when a ViewHolder is in a transient state (e.g. during an
         animation).

         Our ViewHolders are effectively just FrameLayout instances in which we put Fragment
         Views, so it's safe to force recycle them. This is because:
         - FrameLayout instances are not to be directly manipulated, so no animations are
         expected to be running directly on them.
         - Fragment Views are not reused between position (one Fragment = one page). Animation
         running in one of the Fragment Views won't affect another Fragment View.
         - If a user chooses to violate these assumptions, they are also in the position to
         correct the state in their code.
        */
        return true
    }

    private fun removeFragment(itemId: Long) {
        val fragment = mFragments[itemId] ?: return
        if (fragment.view != null) {
            val viewParent = fragment.requireView().parent
            if (viewParent != null) {
                (viewParent as FrameLayout).removeAllViews()
            }
        }
        if (!containsItem(itemId)) {
            mSavedStates.remove(itemId)
        }
        if (!fragment.isAdded) {
            mFragments.remove(itemId)
            return
        }
        if (shouldDelayFragmentTransactions()) {
            mHasStaleFragments = true
            return
        }
        if (fragment.isAdded && containsItem(itemId)) {
            mSavedStates.put(itemId, mFragmentManager.saveFragmentInstanceState(fragment))
        }
        mFragmentManager.beginTransaction().remove(fragment).commitNow()
        mFragments.remove(itemId)
    }

    internal fun  // to avoid creation of a synthetic accessor
            shouldDelayFragmentTransactions(): Boolean {
        return mFragmentManager.isStateSaved
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
    open fun getItemId(position: Int, item: T): Long {
        return position.toLong()
    }

    /**
     * Default implementation works for collections that don't add, move, remove items.
     *
     *
     * TODO(b/122670460): add lint rule
     * When overriding, also override [.getItemId]
     */
    open fun containsItem(itemId: Long): Boolean {
        return itemId >= 0 && itemId < adapter.itemCount
    }

    override fun saveState(): Parcelable {
        /** TODO(b/122670461): use custom [Parcelable] instead of Bundle to save space  */
        val savedState = Bundle(mFragments.size() + mSavedStates.size())
        /** save references to active fragments  */
        for (ix in 0 until mFragments.size()) {
            val itemId = mFragments.keyAt(ix)
            val fragment = mFragments[itemId]
            if (fragment != null && fragment.isAdded) {
                val key = createKey(KEY_PREFIX_FRAGMENT, itemId)
                mFragmentManager.putFragment(savedState, key, fragment)
            }
        }
        /** Write [) into a ][mSavedStates] */
        for (ix in 0 until mSavedStates.size()) {
            val itemId = mSavedStates.keyAt(ix)
            if (containsItem(itemId)) {
                val key = createKey(KEY_PREFIX_STATE, itemId)
                savedState.putParcelable(key, mSavedStates[itemId])
            }
        }
        return savedState
    }

    override fun restoreState(savedState: Parcelable) {
        check(!(!mSavedStates.isEmpty || !mFragments.isEmpty)) { "Expected the adapter to be 'fresh' while restoring state." }
        val bundle = savedState as Bundle
        if (bundle.classLoader == null) {
            /** TODO(b/133752041): pass the class loader from [ViewPager2.SavedState]  */
            bundle.classLoader = javaClass.classLoader
        }
        for (key in bundle.keySet()) {
            if (isValidKey(key, KEY_PREFIX_FRAGMENT)) {
                val itemId = parseIdFromKey(key, KEY_PREFIX_FRAGMENT)
                val fragment = mFragmentManager.getFragment(bundle, key)
                mFragments.put(itemId, fragment)
                continue
            }
            if (isValidKey(key, KEY_PREFIX_STATE)) {
                val itemId = parseIdFromKey(key, KEY_PREFIX_STATE)
                val state = bundle.getParcelableCompat<Fragment.SavedState>(key)
                if (containsItem(itemId)) {
                    mSavedStates.put(itemId, state)
                }
                continue
            }
            throw IllegalArgumentException("Unexpected key in savedState: $key")
        }
        if (!mFragments.isEmpty) {
            mHasStaleFragments = true
            mIsInGracePeriod = true
            gcFragments()
            scheduleGracePeriodEnd()
        }
    }

    private fun scheduleGracePeriodEnd() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            mIsInGracePeriod = false
            gcFragments() // good opportunity to GC
        }
        mLifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(
                source: LifecycleOwner,
                event: Lifecycle.Event
            ) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    handler.removeCallbacks(runnable)
                    source.lifecycle.removeObserver(this)
                }
            }
        })
        handler.postDelayed(runnable, GRACE_WINDOW_TIME_MS)
    }

    // Helper function for dealing with save / restore state
    private fun createKey(prefix: String, id: Long): String {
        return prefix + id
    }

    // Helper function for dealing with save / restore state
    private fun isValidKey(key: String, prefix: String): Boolean {
        return key.startsWith(prefix) && key.length > prefix.length
    }

    // Helper function for dealing with save / restore state
    private fun parseIdFromKey(key: String, prefix: String): Long {
        return key.substring(prefix.length).toLong()
    }

    //<editor-fold desc="生命周期绑定" defaultstatus="collapsed">
    /**
     * Pauses (STARTED) all Fragments that are attached and not a primary item.
     * Keeps primary item Fragment RESUMED.
     */
    internal inner class FragmentMaxLifecycleEnforcer : BaseFragmentMaxLifecycleEnforcer() {
        override val adapter: MultiTypeAdapter
            get() = this@FragmentBinder.adapter
        override val mFragments: LongSparseArray<Fragment>
            get() = this@FragmentBinder.mFragments
        override val mFragmentManager: FragmentManager
            get() = this@FragmentBinder.mFragmentManager
        override val mLifecycle: Lifecycle
            get() = this@FragmentBinder.mLifecycle

        override fun shouldDelayFragmentTransactions(): Boolean {
            return this@FragmentBinder.shouldDelayFragmentTransactions()
        }

        override fun createRecyclerViewAdapterDataObserver(): RecyclerView.AdapterDataObserver {
            return object : DataSetChangeObserver() {
                override fun onChanged() {
                    updateFragmentMaxLifecycle(true)
                }
            }
        }
    }

    /**
     * Simplified [RecyclerView.AdapterDataObserver] for clients interested in any data-set
     * changes regardless of their nature.
     */
    private abstract class DataSetChangeObserver : RecyclerView.AdapterDataObserver() {
        abstract override fun onChanged()
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeChanged(
            positionStart: Int, itemCount: Int,
            payload: Any?
        ) {
            onChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            onChanged()
        }
    }
    //</editor-fold>

    companion object {
        private const val TAG = "KW_FragmentBinder"

        // State saving config
        private const val KEY_PREFIX_FRAGMENT = "f#"
        private const val KEY_PREFIX_STATE = "s#"

        // Fragment GC config
        private const val GRACE_WINDOW_TIME_MS: Long = 10000 // 10 seconds
    }
}
