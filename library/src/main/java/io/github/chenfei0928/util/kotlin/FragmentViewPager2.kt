package io.github.chenfei0928.util.kotlin

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import io.github.chenfei0928.util.Log

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-04-19 15:20
 */
private const val TAG = "KW_FragmentViewPager2"

fun ViewPager2.findFragment(
    fragmentManager: FragmentManager, position: Int = currentItem
): Fragment? {
    val adapter = adapter as? FragmentStateAdapter ?: return null
    return when {
        adapter.itemCount == 0 -> null
        adapter.itemCount <= position -> {
            Log.e(
                TAG, "findFragment: ", IndexOutOfBoundsException(
                    "adapter.count less to viewPager.currentItem: $adapter ${adapter.itemCount}, $position"
                )
            )
            return null
        }
        else -> findCurrentFragmentByStateAdapter(adapter, fragmentManager, position)
    }
}

/**
 * 从[FragmentPagerAdapter]中查找当前显示的Fragment
 */
private fun ViewPager2.findCurrentFragmentByStateAdapter(
    adapter: FragmentStateAdapter, fm: FragmentManager, position: Int = currentItem
): Fragment? {
    val id = adapter.getItemId(position)
    val name = makeFragmentTag(id)
    return fm.findFragmentByTag(name)
}

/**
 * 获取Fragment在ViewPager中的Tag，实现完全复制自
 * [androidx.viewpager2.adapter.FragmentStateAdapter.placeFragmentInViewHolder]
 */
private fun makeFragmentTag(id: Long): String {
    return "f$id"
}
