package io.github.chenfei0928.util.mediaStore

import android.os.Build
import androidx.activity.result.launch
import io.github.chenfei0928.util.R
import io.github.chenfei0928.util.PermissionRequest
import io.github.chenfei0928.util.kotlin.onPermissionDeniedKt
import io.github.chenfei0928.util.kotlin.onPermissionNeverAskAgainKt
import io.github.chenfei0928.util.kotlin.onShowPermissionRationale
import io.github.chenfei0928.util.kotlin.registerForPermission

/**
 * 支持权限检查、请求处理的文件导入器
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-23 15:23
 */
abstract class BasePermissionFileImportFragment<T>(
    requestPermission: Array<String>
) : BaseFileImportFragment<T>() {
    open val permissionMaxSdkVersion: Int = Int.MAX_VALUE
    abstract val permissionName: String

    private val sdCardPermissionLauncher = registerForPermission(
        permissions = requestPermission,
        onRationale = ::showRationaleForPermission,
        onAgree = ::launchFileChooseImpl,
        onDenied = ::onPermissionDenied,
        onNeverAskAgain = ::onPermissionNeverAskAgain
    )

    final override fun launchFileChoose() {
        if (permissionMaxSdkVersion <= Build.VERSION.SDK_INT) {
            // 如果当前sdk等级已经不再需要该权限，不请求
            launchFileChooseImpl()
        } else {
            sdCardPermissionLauncher.launch()
        }
    }

    protected abstract fun launchFileChooseImpl()

    /**
     * 显示权限原因
     */
    private fun showRationaleForPermission(request: PermissionRequest) {
        requireActivity().onShowPermissionRationale(
            getString(
                R.string.permissionRationale, permissionName
            ), request
        )
    }

    /**
     * 当权限被拒绝
     */
    private fun onPermissionDenied() {
        requireActivity().onPermissionDeniedKt(getString(R.string.permissionDenied, permissionName),
            { _, _ -> removeSelf(null) },
            { _, _ -> removeSelf(null) })
    }

    /**
     * 当前线被不再提醒
     */
    private fun onPermissionNeverAskAgain() {
        requireActivity().onPermissionNeverAskAgainKt(
            getString(
                R.string.permissionNeverAskAgain, permissionName
            )
        ) { _, _ ->
            removeSelf(null)
        }
    }
}

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
