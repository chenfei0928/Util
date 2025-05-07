package androidx.fragment.app

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import java.lang.reflect.Field
import java.util.WeakHashMap

/**
 * 用于从FragmentView生命周期中获取其所对应Fragment
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-02-17 17:25
 */
internal object FragmentViewLifecycleCf0928UtilAccessor : FragmentManager.FragmentLifecycleCallbacks() {
    private val viewLifecycleOwnerFragmentMap: MutableMap<LifecycleOwner, Fragment> = WeakHashMap()

    private val mFragmentField: Field = FragmentViewLifecycleOwner::class.java
        .getDeclaredField("mFragment").apply {
            isAccessible = true
        }

    fun isViewLifecycleOwner(target: Any?): Boolean = target is FragmentViewLifecycleOwner

    fun getFragmentByViewLifecycleOwner(fragmentLifecycleOwner: LifecycleOwner): Fragment {
        fragmentLifecycleOwner as FragmentViewLifecycleOwner
        return viewLifecycleOwnerFragmentMap[fragmentLifecycleOwner]
            ?: mFragmentField.get(fragmentLifecycleOwner) as Fragment
    }

    override fun onFragmentViewCreated(
        fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?
    ) {
        // view创建之后保存下来viewLifecycleOwner与其所在fragment的映射
        viewLifecycleOwnerFragmentMap[f.viewLifecycleOwner] = f
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentViewDestroyed(fm, f)
        // view销毁之后主动移除该fragment的映射
        viewLifecycleOwnerFragmentMap.filterValues { it === f }.map {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                viewLifecycleOwnerFragmentMap.remove(it.key, it.value)
            } else {
                viewLifecycleOwnerFragmentMap.remove(it.key)
            }
        }
    }
}
