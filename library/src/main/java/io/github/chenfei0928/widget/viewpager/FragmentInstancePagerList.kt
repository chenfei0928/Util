package io.github.chenfei0928.widget.viewpager

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-03-15 13:34
 */
interface FragmentInstancePagerList {

    //<editor-fold defaultstate="collapsed" desc="增删改查">
    fun <T : Fragment> append(
        clazz: Class<T>, title: CharSequence?, creator: () -> T
    )

    /**
     * 追加一个Fragment
     *
     * @param index   追加的Fragment所处的位置，默认为追加到最后
     * @param clazz   Fragment类型的Class
     * @param title   标题
     * @param creator 创建该Fragment实例
     */
    fun <T : Fragment> append(
        index: Int = this.getItemCount(), clazz: Class<T>, title: CharSequence?, creator: () -> T
    )

    /**
     * 移除指定类型的Fragment
     */
    fun <T : Fragment> removeByType(clazz: Class<T>)

    fun clear()

    /**
     * 获取指定类型Fragment的下标
     */
    fun <T : Fragment> indexOfType(clazz: Class<T>): Int

    /**
     * 获取指定位置的fragment类型
     * （非实例，获取实例请根据其位置下标使用[com.yikelive.util.kotlin.findFragmentWithType]来获取实例）
     */
    fun getTypeByIndex(index: Int): Class<out Fragment>
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="适配器父类方法">
    fun createFragment(position: Int): Fragment

    fun getItemCount(): Int

    fun getPageTitle(position: Int): CharSequence?
    //</editor-fold>
}

inline fun <reified T : Fragment> FragmentInstancePagerList.append(
    title: CharSequence?, noinline creator: () -> T
) {
    append(clazz = T::class.java, title = title, creator = creator)
}

inline fun <reified T : Fragment> FragmentInstancePagerList.indexOfType(): Int {
    return indexOfType(T::class.java)
}

inline fun <reified T : Fragment> FragmentInstancePagerList.removeByType() {
    removeByType(T::class.java)
}

internal class FragmentInstancePagerListImpl : FragmentInstancePagerList {
    internal val list = mutableListOf<AdapterFragmentPager<*>>()

    //<editor-fold defaultstate="collapsed" desc="适配器父类方法">
    override fun createFragment(position: Int): Fragment {
        val adapterFragmentPager = list[position]
        return adapterFragmentPager
            .creator()
            .also {
                adapterFragmentPager.argument = it.arguments
            }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return list[position].title
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="增删改查">
    override fun <T : Fragment> append(clazz: Class<T>, title: CharSequence?, creator: () -> T) {
        append(getItemCount(), clazz, title, creator)
    }

    /**
     * 追加一个Fragment
     *
     * @param index   追加的Fragment所处的位置，默认为追加到最后
     * @param clazz   Fragment类型的Class
     * @param title   标题
     * @param creator 创建该Fragment实例
     */
    override fun <T : Fragment> append(
        index: Int, clazz: Class<T>, title: CharSequence?, creator: () -> T
    ) {
        list.add(index, AdapterFragmentPager(clazz, title, creator))
    }

    /**
     * 移除指定类型的Fragment
     */
    override fun <T : Fragment> removeByType(clazz: Class<T>) {
        list.removeAll { it.clazz == clazz }
        // notifyDataSetChanged()
    }

    override fun clear() {
        list.clear()
    }

    /**
     * 获取指定类型Fragment的下标
     */
    override fun <T : Fragment> indexOfType(clazz: Class<T>): Int {
        return list.indexOfFirst { it.clazz == clazz }
    }

    /**
     * 获取指定位置的fragment类型
     * （非实例，获取实例请根据其位置下标使用[io.github.chenfei0928.app.fragment.findFragmentWithType]来获取实例）
     */
    override fun getTypeByIndex(index: Int): Class<out Fragment> {
        return list[index].clazz
    }
    //</editor-fold>

    internal class AdapterFragmentPager<T : Fragment>(
        val clazz: Class<T>,
        val title: CharSequence?,
        val creator: () -> T,
        var argument: Bundle? = null
    )
}
