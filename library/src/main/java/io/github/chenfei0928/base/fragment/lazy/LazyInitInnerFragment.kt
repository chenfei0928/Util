package io.github.chenfei0928.base.fragment.lazy

import androidx.fragment.app.Fragment
import io.github.chenfei0928.reflect.parameterized.getParentParameterizedTypeClassDefinedImplInChild
import io.github.chenfei0928.util.R

/**
 * 子fragment的懒加载fragment，只会延时载入子fragment
 * 子fragment的view加载由fragment框架负责在主线程中初始化
 *
 * 使用时必须通过以下三种方式提供内容[F]类型：
 * - 不重写子类但通过构造器提供类实例
 * - 重写[createFragment]方法返回新实例
 * - 重写一个子类提供具体泛型，供[getParentParameterizedTypeClassDefinedImplInChild]反射获取
 */
open class LazyInitInnerFragment<F : Fragment>(
    childFragmentClass: Class<F>? = null
) : BaseDoubleCheckLazyInitFragment() {
    val fragment: F by lazy {
        @Suppress("UNCHECKED_CAST")
        childFragmentManager.findFragmentByTag(FRAGMENT_TAG) as? F
            ?: createFragment()
            ?: (childFragmentClass ?: getParentParameterizedTypeClassDefinedImplInChild(0))
                .getDeclaredConstructor()
                .newInstance()
    }

    final override fun checkInflateImpl() {
        if (!isInflated) {
            childFragmentManager
                .beginTransaction()
                .add(R.id.lazyInitPlaceHolder, fragment, FRAGMENT_TAG)
                .commitNowAllowingStateLoss()
        }
    }

    override val isInflated: Boolean
        get() = fragment.isAdded

    protected open fun createFragment(): F? = null

    companion object {
        private const val FRAGMENT_TAG = "inner"
    }
}
