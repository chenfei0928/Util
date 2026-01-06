package io.github.chenfei0928.app.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import io.github.chenfei0928.util.Log

private const val TAG = "Ut_FragmentViewPager"

/**
 * 获取显示在ViewPager中指定位置的fragment实例（如果指定位置的fragment未被加载，将返回null）
 */
inline fun <reified F> ViewPager.findFragmentWithType(
    fm: FragmentManager, position: Int = currentItem
): F? {
    return findFragment(fm, position) as? F
}

/**
 * 从ViewPager中获取当前显示的Fragment。由于fragmentViewPagerAdapter的缓存机制，
 * 故不应该去传入实例以每次[FragmentPagerAdapter.getItem]时返回实例，而是在该方法中创建并返回。
 *
 * [FragmentPagerAdapter.instantiateItem]有根据ViewPager的id与itemId来生成tag：
 * [FragmentPagerAdapter.makeFragmentName]，以从FragmentManager中[FragmentManager.findFragmentByTag]
 * 获取被加载过的fragment的流程。
 * [FragmentStatePagerAdapter.instantiateItem]的从自身[FragmentStatePagerAdapter.mFragments]缓存中获取。
 */
@JvmOverloads
fun ViewPager.findFragment(fm: FragmentManager, position: Int = currentItem): Fragment? {
    val adapter = adapter
    return when {
        adapter == null -> null
        adapter.count == 0 -> null
        adapter.count <= position -> {
            Log.e(TAG, "findFragment: ", run {
                IndexOutOfBoundsException(
                    "adapter.count less to viewPager.currentItem: $adapter ${adapter.count}, $position"
                )
            })
            return null
        }
        adapter is FragmentPagerAdapter -> findCurrentFragmentByAdapter(adapter, fm, position)
        adapter is FragmentStatePagerAdapter -> findCurrentFragmentByStatePager(
            adapter, fm, position
        )
        else -> null
    }
}

/**
 * 从[FragmentStatePagerAdapter]中查找当前显示的Fragment
 * 要求adapter实现[FragmentStatePagerAdapter.getItemPosition]
 */
private fun ViewPager.findCurrentFragmentByStatePager(
    adapter: FragmentStatePagerAdapter, fm: FragmentManager, position: Int
): Fragment? {
    return fm.fragments.find {
        it.id == this.id && adapter.getItemPosition(it) == position
    }
}

/**
 * 从[FragmentPagerAdapter]中查找当前显示的Fragment
 */
private fun ViewPager.findCurrentFragmentByAdapter(
    adapter: FragmentPagerAdapter, fm: FragmentManager, position: Int
): Fragment? {
    val id = adapter.getItemId(position)
    val name = makeFragmentName(this.id, id)
    return fm.findFragmentByTag(name)
}

/**
 * 获取Fragment在ViewPager中的Tag，实现完全复制自
 * [androidx.fragment.app.FragmentPagerAdapter.makeFragmentName]
 */
private fun makeFragmentName(viewId: Int, id: Long): String {
    return "android:switcher:$viewId:$id"
}
