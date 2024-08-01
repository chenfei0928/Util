package androidx.viewpager2.adapter

import androidx.collection.LongSparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.drakeet.multitype.MultiTypeAdapter

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2023-05-31 21:59
 */
abstract class BaseFragmentMaxLifecycleEnforcer {
    private var mPageChangeCallback: ViewPager2.OnPageChangeCallback? = null
    private var mDataObserver: RecyclerView.AdapterDataObserver? = null
    private var mLifecycleObserver: LifecycleEventObserver? = null
    private var mViewPager: ViewPager2? = null
    private var mPrimaryItemId = RecyclerView.NO_ID

    fun register(recyclerView: RecyclerView) {
        mViewPager = inferViewPager(recyclerView)

        // signal 1 of 3: current item has changed
        mPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                updateFragmentMaxLifecycle(false)
            }

            override fun onPageSelected(position: Int) {
                updateFragmentMaxLifecycle(false)
            }
        }
        mViewPager!!.registerOnPageChangeCallback(mPageChangeCallback!!)

        // signal 2 of 3: underlying data-set has been updated
        mDataObserver = createRecyclerViewAdapterDataObserver()
        adapter.registerAdapterDataObserver(mDataObserver!!)

        // signal 3 of 3: we may have to catch-up after being in a lifecycle state that
        // prevented us to perform transactions
        mLifecycleObserver = LifecycleEventObserver { _, _ ->
            updateFragmentMaxLifecycle(false)
        }
        mLifecycle.addObserver(mLifecycleObserver!!)
    }

    fun unregister(recyclerView: RecyclerView) {
        val viewPager = inferViewPager(recyclerView)
        viewPager.unregisterOnPageChangeCallback(mPageChangeCallback!!)
        adapter.unregisterAdapterDataObserver(mDataObserver!!)
        mLifecycle.removeObserver(mLifecycleObserver!!)
        mViewPager = null
    }

    fun updateFragmentMaxLifecycle(dataSetChanged: Boolean) {
        if (shouldDelayFragmentTransactions()) {
            return
            /** recovery step via [.mLifecycleObserver]  */
        }
        if (mViewPager!!.scrollState != ViewPager2.SCROLL_STATE_IDLE) {
            return  // do not update while not idle to avoid jitter
        }
        if (mFragments.isEmpty() || adapter.getItemCount() == 0) {
            return  // nothing to do
        }
        val currentItem = mViewPager!!.currentItem
        if (currentItem >= adapter.getItemCount()) {
            /** current item is yet to be updated; it is guaranteed to change, so we will be
             * notified via [ViewPager2.OnPageChangeCallback.onPageSelected]   */
            return
        }
        val currentItemId: Long = adapter.getItemId(currentItem)
        if (currentItemId == mPrimaryItemId && !dataSetChanged) {
            return  // nothing to do
        }
        // 此处不判断此逻辑，否则可能会导致从fragment到binder切换时导致fragment不pause
//        val currentItemFragment: Fragment? = mFragments.get(currentItemId)
//        if (currentItemFragment == null || !currentItemFragment.isAdded) {
//            return
//        }
        mPrimaryItemId = currentItemId
        val transaction: FragmentTransaction = mFragmentManager.beginTransaction()
        var toResume: Fragment? = null
        for (ix in 0 until mFragments.size()) {
            val itemId: Long = mFragments.keyAt(ix)
            val fragment: Fragment = mFragments.valueAt(ix)
            if (!fragment.isAdded) {
                continue
            }
            if (itemId != mPrimaryItemId) {
                transaction.setMaxLifecycle(fragment, Lifecycle.State.STARTED)
            } else {
                toResume = fragment // itemId map key, so only one can match the predicate
            }
            fragment.setMenuVisibility(itemId == mPrimaryItemId)
        }
        if (toResume != null) { // in case the Fragment wasn't added yet
            transaction.setMaxLifecycle(toResume, Lifecycle.State.RESUMED)
        }
        if (!transaction.isEmpty) {
            transaction.commitNow()
        }
    }

    private fun inferViewPager(recyclerView: RecyclerView): ViewPager2 {
        val parent = recyclerView.parent
        if (parent is ViewPager2) {
            return parent
        }
        throw IllegalStateException("Expected ViewPager2 instance. Got: $parent")
    }

    protected abstract val adapter: MultiTypeAdapter
    protected abstract val mFragments: LongSparseArray<Fragment>
    protected abstract val mFragmentManager: FragmentManager
    protected abstract val mLifecycle: Lifecycle
    protected abstract fun shouldDelayFragmentTransactions(): Boolean
    protected abstract fun createRecyclerViewAdapterDataObserver(): RecyclerView.AdapterDataObserver
}
