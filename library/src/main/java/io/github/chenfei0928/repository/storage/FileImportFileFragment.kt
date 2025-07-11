package io.github.chenfei0928.repository.storage

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.UiThread
import androidx.fragment.app.commit
import io.github.chenfei0928.app.ProgressDialog
import io.github.chenfei0928.concurrent.coroutines.coroutineScope
import io.github.chenfei0928.concurrent.coroutines.showWithContext
import io.github.chenfei0928.content.PictureUriUtil
import io.github.chenfei0928.io.FileUtil
import io.github.chenfei0928.os.safeHandler
import io.github.chenfei0928.util.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * 文件导入处理，返回文件的file，由于涉及到直接获取文件file，而非其uri，
 * 则可能在低版本系统中需要权限才能在后续处理中成功读取文件
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-19 12:49
 */
class FileImportFileFragment : BasePermissionFileImportFragment<File>(
    requestPermission = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    )
) {
    // Android10之后只允许使用SAF访问文件，不再需要扩展卡权限
    override val permissionMaxSdkVersion: Int = Build.VERSION_CODES.Q
    override val permissionName: String
        get() = getString(R.string.cf0928util_permissionName_sdcard)

    @UiThread
    override fun launchFileChooseImpl() {
        val arguments = arguments
        val implClass = arguments?.getString(KEY_IMPL_CLASS) ?: run {
            removeSelf(null)
            return
        }
        val implArg = arguments.getBundle(KEY_IMPL_ARG)
        // 创建implFragment
        @Suppress("UNCHECKED_CAST")
        val implFragment = childFragmentManager.fragmentFactory.instantiate(
            this.javaClass.classLoader!!, implClass
        ) as BaseFileImportFragment<Uri>
        // 为implFragment传递参数和回调
        implFragment.arguments = implArg
        implFragment.resultCallback = { uri ->
            copyAsFileToRemoveSelf(uri)
        }
        // 添加implFragment
        childFragmentManager.commit {
            add(implFragment, "implFragment")
        }
    }

    private fun copyAsFileToRemoveSelf(uri: Uri?) {
        safeHandler.post {
            val context = context
            // 判断系统版本，并复制到缓存文件夹中或解析其原始文件路径
            if (uri == null || context == null) {
                removeSelf(null)
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // 获得文件路径
                val path = PictureUriUtil.getPath(requireContext(), uri)
                removeSelf(File(path))
            } else {
                // 高于Android10，复制到缓存文件夹中
                coroutineScope.launch {
                    val tmpFile = ProgressDialog(context).apply {
                        // 显示进度提示
                        setMessage(getString(R.string.cf0928util_toast_extStrong_fileImporting))
                        setCanceledOnTouchOutside(false)
                        setCancelable(false)
                        // 在io线程中复制文件
                    }.showWithContext(Dispatchers.IO) {
                        // 创建临时文件
                        val extName = FileUtil.getFileExtensionFromUrl(uri.toString())
                        val tmpFile =
                            File(context.cacheDir, "${System.currentTimeMillis()}.$extName")
                        // 如果文件复制成功，返回该文件
                        if (FileUtil.copyUriToDestFile(requireContext(), uri, tmpFile)) {
                            tmpFile
                        } else {
                            null
                        }
                    }
                    // 通知回调，并移除自身
                    removeSelf(tmpFile)
                }
            }
        }
    }

    companion object {
        private const val KEY_IMPL_CLASS = "implClass"
        private const val KEY_IMPL_ARG = "implArg"

        fun <F : BaseFileImportFragment<Uri>> newInstance(clazz: Class<F>, implArg: Bundle) =
            FileImportFileFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_IMPL_CLASS, clazz.name)
                    putBundle(KEY_IMPL_ARG, implArg)
                }
            }
    }
}
