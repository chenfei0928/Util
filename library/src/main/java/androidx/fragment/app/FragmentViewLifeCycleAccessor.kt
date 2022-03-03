package androidx.fragment.app

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import java.lang.reflect.Field
import java.util.*

/**
 * 用于从FragmentView生命周期中获取其所对应Fragment
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-02-17 17:25
 */
internal object FragmentViewLifeCycleAccessor : FragmentManager.FragmentLifecycleCallbacks() {
    private val viewLifecycleOwnerFragmentMap = WeakHashMap<LifecycleOwner, Fragment>()

    private val mFragmentField: Field = FragmentViewLifecycleOwner::class.java
        .getDeclaredField("mFragment").apply {
            isAccessible = true
        }

    fun isInstance(target: Any?): Boolean = target is FragmentViewLifecycleOwner

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
        // view销毁之后主动移除viewLifecycleOwner与其所在fragment的映射
        viewLifecycleOwnerFragmentMap[f.viewLifecycleOwner] = f
    }
}
