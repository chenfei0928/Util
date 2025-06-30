package io.github.chenfei0928.widget.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import io.github.chenfei0928.content.contentEquals

/**
 * 为每个类型 Fragment 都只出现一个的 ViewPager 准备的 Adapter
 * [getItem] 方法需每次返回一个全新的fragment实例，缓存复用逻辑由框架负责
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-06-13 15:57
 */
@Deprecated("使用 ViewPager2 与 FragmentInstancePager2Adapter")
class FragmentInstancePagerAdapter
@JvmOverloads constructor(
    fm: FragmentManager,
    private val pagerList: FragmentInstancePagerList = FragmentInstancePagerListImpl()
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT),
    FragmentInstancePagerList by pagerList {

    //<editor-fold defaultstate="collapsed" desc="适配器父类方法">
    override fun getItem(position: Int): Fragment {
        return createFragment(position)
    }

    override fun getCount(): Int {
        return getItemCount()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return pagerList.getPageTitle(position)
    }

    override fun getItemPosition(pageFragment: Any): Int {
        if (pageFragment !is Fragment || pagerList !is FragmentInstancePagerListImpl) {
            return POSITION_NONE
        }
        val indexOfFirst = pagerList.list.indexOfFirst {
            checkPageFragmentInfo(pageFragment, it)
        }
        return if (indexOfFirst >= 0) {
            indexOfFirst
        } else {
            POSITION_NONE
        }
    }
    //</editor-fold>

    private fun checkPageFragmentInfo(
        pageFragment: Fragment, pageInfo: FragmentInstancePagerListImpl.AdapterFragmentPager<*>
    ): Boolean {
        // 判断是否是相同类型
        if (!pageInfo.clazz.isInstance(pageFragment)) {
            return false
        }
        // 校验参数
        val argument = pageInfo.argument
        val pageArgument = pageFragment.arguments
        return if (argument === pageArgument) {
            true
        } else if (argument == null || pageArgument == null) {
            false
        } else {
            argument.contentEquals(pageArgument)
        }
    }
}
