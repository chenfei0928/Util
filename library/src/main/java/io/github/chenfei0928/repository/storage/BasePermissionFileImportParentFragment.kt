package io.github.chenfei0928.repository.storage

/**
 * 子类提供fragment，该类用于请求权限
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-23 16:06
 */
abstract class BasePermissionFileImportParentFragment<T>(
    requestPermission: Array<String>
) : BasePermissionFileImportFragment<T>(requestPermission) {

    override fun launchFileChooseImpl() {
        // 创建implFragment
        val implFragment = createFragment() ?: run {
            removeSelf(null)
            return
        }
        // 为implFragment传递回调
        implFragment.resultCallback = { uri ->
            removeSelf(uri)
        }
        // 添加implFragment
        childFragmentManager
            .beginTransaction()
            .add(implFragment, "implFragment")
            .commit()
    }

    abstract fun createFragment(): BaseFileImportFragment<T>?
}
