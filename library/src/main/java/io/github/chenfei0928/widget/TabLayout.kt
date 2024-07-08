package io.github.chenfei0928.widget

import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import io.github.chenfei0928.reflect.ListenersProxy
import io.github.chenfei0928.view.asyncinflater.LikeListViewInjector

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-05-24 13:59
 */
class TabLayout : LinearLayout {
    lateinit var childTabViewCreator: LikeListViewInjector.Adapter<TabLayout, TabState>
    private var privateOnTabSelectListener: OnTabSelectListener =
        ListenersProxy.newEmptyListener(OnTabSelectListener::class.java)
    val onTabSelectListeners: MutableList<OnTabSelectListener> =
        mutableListOf(ListenersProxy.newImplByGetter(this::privateOnTabSelectListener))
    private val internalOnTabSelectListener: OnTabSelectListener =
        ListenersProxy.newListenersProxy(OnTabSelectListener::class.java, onTabSelectListeners)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    //<editor-fold desc="ViewPager1" defaultstate="collapsed">
    private var viewPager: ViewPager? = null

    private var onPageChangeLis: ViewPager.OnPageChangeListener =
        object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                currentTab = position
            }
        }
    private var onAdapterChangeLis: ViewPager.OnAdapterChangeListener =
        ViewPager.OnAdapterChangeListener { viewPager, oldAdapter, newAdapter ->
            if (oldAdapter == newAdapter) {
                return@OnAdapterChangeListener
            }
            oldAdapter?.unregisterDataSetObserver(dataSetObs)
            newAdapter?.registerDataSetObserver(dataSetObs)
            newAdapter?.let {
                currentTab = viewPager.currentItem
            }
        }
    private var dataSetObs: DataSetObserver = object : DataSetObserver() {
        override fun onChanged() {
            val viewPager = viewPager
            viewPager?.adapter?.let {
                currentTab = viewPager.currentItem
            }
        }
    }

    fun setupWithViewPager(target: ViewPager, autoRefresh: Boolean) {
        if (viewPager == target) {
            return
        }
        viewPager?.let {
            it.removeOnPageChangeListener(onPageChangeLis)
            it.removeOnAdapterChangeListener(onAdapterChangeLis)
            it.adapter?.unregisterDataSetObserver(dataSetObs)
        }
        viewPager = target
        val adapter =
            target.adapter ?: throw IllegalArgumentException("target ViewPager Adapter is null")
        if (adapter.count > 0) {
            currentTab = target.currentItem
        }
        privateOnTabSelectListener = object : OnTabSelectListener {
            override fun onTabSelect(view: View, position: Int) {
                target.currentItem = position
            }

            override fun onTabReselect(view: View, position: Int) {
                // noop
            }
        }
        target.addOnPageChangeListener(onPageChangeLis)
        if (autoRefresh) {
            target.addOnAdapterChangeListener(onAdapterChangeLis)
            adapter.registerDataSetObserver(dataSetObs)
        }
    }

    private fun setupWithAdapter(viewPager: ViewPager, adapter: PagerAdapter) {
        LikeListViewInjector.inject(this,
            (0 until adapter.count).map { TabState(it, viewPager.currentItem == it) },
            object : LikeListViewInjector.Adapter<TabLayout, TabState> {
                override fun onCreateView(inflater: LayoutInflater, parent: TabLayout): View {
                    val childTabView = childTabViewCreator.onCreateView(inflater, parent)
                    childTabView.setOnClickListener {
                        val indexOfChild = this@TabLayout.indexOfChild(childTabView)
                        if (it.isSelected) {
                            internalOnTabSelectListener.onTabReselect(childTabView, indexOfChild)
                        } else {
                            internalOnTabSelectListener.onTabSelect(childTabView, indexOfChild)
                        }
                    }
                    return childTabView
                }

                override fun onBindView(view: View, bean: TabState) {
                    childTabViewCreator.onBindView(view, bean)
                }
            })
        visibility = if (adapter.count <= 1) {
            hideSelfWhenOnlyOneTab
        } else {
            View.VISIBLE
        }
    }
    //</editor-fold>

    var hideSelfWhenOnlyOneTab = View.VISIBLE
    var currentTab: Int = 0
        set(value) {
            setupWithAdapter(viewPager!!, viewPager!!.adapter!!)
            field = value
        }

    data class TabState(
        val position: Int, val isSelected: Boolean
    )

    interface OnTabSelectListener {
        fun onTabSelect(view: View, position: Int)
        fun onTabReselect(view: View, position: Int)
    }
}
