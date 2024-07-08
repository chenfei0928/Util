package io.github.chenfei0928.widget.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.jetbrains.anko.collections.forEachReversedWithIndex

/**
 * 用于[androidx.viewpager2.widget.ViewPager2]的[FragmentInstancePagerAdapter]
 *
 * 为每个类型 Fragment 都只出现一个的 ViewPager 准备的 Adapter
 * [createFragment] 方法需每次返回一个全新的fragment实例，缓存复用逻辑由框架负责
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-03-15 13:26
 */
class FragmentInstancePager2Adapter
@JvmOverloads constructor(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val pagerList: FragmentInstancePagerList = FragmentInstancePagerListImpl()
) : FragmentStateAdapter(fragmentManager, lifecycle), FragmentInstancePagerList by pagerList {

    //<editor-fold defaultstate="collapsed" desc="构造方法">
    constructor(fragmentActivity: FragmentActivity) : this(
        fragmentActivity.supportFragmentManager, fragmentActivity.lifecycle
    )

    constructor(fragment: Fragment) : this(fragment.childFragmentManager, fragment.lifecycle)
    //</editor-fold>

    override fun <T : Fragment> append(
        index: Int, clazz: Class<T>, title: CharSequence?, creator: () -> T
    ) {
        pagerList.append(index, clazz, title, creator)
        notifyItemInserted(index)
    }

    override fun <T : Fragment> removeByType(clazz: Class<T>) {
        if (pagerList is FragmentInstancePagerListImpl) {
            pagerList.list.forEachReversedWithIndex { index, e ->
                if (e.clazz == clazz) {
                    pagerList.list.removeAt(index)
                    notifyItemRemoved(index)
                }
            }
        } else {
            pagerList.removeByType(clazz)
        }
    }
}
