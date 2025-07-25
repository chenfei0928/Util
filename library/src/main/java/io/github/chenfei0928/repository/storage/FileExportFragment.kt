package io.github.chenfei0928.repository.storage

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import io.github.chenfei0928.app.fragment.removeSelf
import io.github.chenfei0928.app.result.registerForExternalStoragePermission
import io.github.chenfei0928.base.fragment.BaseFragment
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.concurrent.UiTaskExecutor.Companion.runOnUiThread
import io.github.chenfei0928.concurrent.coroutines.coroutineScope
import io.github.chenfei0928.content.FileProviderUtil
import io.github.chenfei0928.io.FileUtil
import io.github.chenfei0928.os.getParcelableCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * 文件导出处理
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-10 17:38
 */
class FileExportFragment : BaseFragment() {
    private val sdCardPermissionLauncher = registerForExternalStoragePermission {
        if (it) {
            parseArgToSave(requireContext(), requireArguments())
        } else {
            removeSelfAndCallback()
        }
    }
    var resultCallback: (url: Uri?) -> Unit = { }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val bundle = arguments ?: run {
            removeSelfAndCallback()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            coroutineScope.launch(Dispatchers.Default) {
                parseArgToSave(context, bundle)
            }
        } else {
            sdCardPermissionLauncher.launch(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun parseArgToSave(context: Context, bundle: Bundle) {
        val writerClassName = bundle.getString(KEY_CONTENT_WRITER_CLASS) ?: run {
            removeSelfAndCallback()
            return
        }
        // 实例化内容写入器，解析参数
        val writer = Class.forName(writerClassName)
            .getDeclaredConstructor()
            .newInstance() as ContentValuesWriter
        // 解析参数
        if (!writer.parseArg(this, bundle)) {
            removeSelfAndCallback()
            return
        }
        // 根据不同系统版本使用不同的方式来保存文件
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parseArgSaveContentResolver(context, bundle, writer)
        } else {
            parseArgSaveToSdCard(context, bundle, writer)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun parseArgSaveContentResolver(
        context: Context, bundle: Bundle, writer: ContentValuesWriter
    ) {
        val uri = bundle.getParcelableCompat<Uri>(KEY_CONTENT_TYPE) ?: run {
            removeSelfAndCallback()
            return
        }
        val contentValues = bundle.getParcelableCompat<ContentValues>(KEY_CONTENT_VALUES) ?: run {
            removeSelfAndCallback()
            return
        }
        // 写入数据
        val saved = FileResolver.save(context, uri, contentValues, writer)
        removeSelfAndCallback(saved)
    }

    /**
     * 低版本系统（低于 10）中使用，直接保存到扩展卡中并发送通知
     */
    @RequiresPermission(
        allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE]
    )
    private fun parseArgSaveToSdCard(
        context: Context, bundle: Bundle, writer: ContentValuesWriter
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val targetFile = bundle.getString(KEY_TARGET_FILE) ?: run {
                removeSelfAndCallback()
                return@launch
            }
            // 解析参数
            if (!writer.parseArg(this@FileExportFragment, bundle)) {
                removeSelfAndCallback()
                return@launch
            }
            val file = File(targetFile)
            val uri = if (FileResolver.save(context, file, writer)) {
                FileProviderUtil.createUriFromFile(context, file)
            } else {
                null
            }
            removeSelfAndCallback(uri)
        }
    }

    /**
     * 提示用户是否成功保存，并移除自身
     */
    private fun removeSelfAndCallback(uri: Uri? = null) {
        ExecutorUtil.runOnUiThread {
            resultCallback(uri)
            removeSelf()
        }
    }

    companion object {
        /**
         * 用于在高版本系统中使用，用于指定保存路径
         */
        @RequiresApi(Build.VERSION_CODES.Q)
        private const val KEY_CONTENT_TYPE = "contentType"

        @RequiresApi(Build.VERSION_CODES.Q)
        private const val KEY_CONTENT_VALUES = "contentValues"

        /**
         * 只在低于Q版本的系统中使用，保存到指定目录
         */
        private const val KEY_TARGET_FILE = "targetFile"
        private const val KEY_CONTENT_WRITER_CLASS = "contentWriterClass"

        /**
         * 创建文件导出实例
         *
         * @param writerClass 文件写入操作者类
         * @param writerBundle 文件写入操作者参数
         * @param contentType 文件要保存到的url路径（Android 10以上使用）
         * @param contentValues 要保存的文件描述（Android 10以上使用）
         * @param targetFile 低版本系统中文件要保存到的路径
         */
        fun <Writer : ContentValuesWriter> newInstance(
            writerClass: Class<Writer>,
            writerBundle: Bundle?,
            contentType: Uri,
            contentValues: ContentValues,
            targetFile: String
        ): FileExportFragment {
            return FileExportFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_CONTENT_WRITER_CLASS, writerClass.name)
                    putParcelable(KEY_CONTENT_TYPE, contentType)
                    putParcelable(KEY_CONTENT_VALUES, contentValues)
                    putString(KEY_TARGET_FILE, targetFile)
                    if (writerBundle != null) {
                        putAll(writerBundle)
                    }
                }
            }
        }

        /**
         * 为图片保存创建文件导出实例
         *
         * @param writerClass 文件写入操作者类
         * @param writerBundle 文件写入操作者参数
         * @param title 文件标题
         * @param description 文件描述
         * @param displayName 文件名
         * @param relativePath 文件保存相对父路径（通常用于标示不同的应用区分）
         */
        @Suppress("LongParameterList")
        fun <Writer : ContentValuesWriter> newInstanceForImage(
            writerClass: Class<Writer>,
            writerBundle: Bundle?,
            title: String,
            description: String,
            displayName: String,
            relativePath: String
        ): FileExportFragment {
            val extName = FileUtil.getFileExtensionFromUrl(displayName)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extName)
            // 高版本的数据描述
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, title)
            values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            values.put(MediaStore.Images.Media.DESCRIPTION, description)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$relativePath")
            // 低版本的保存路径
            val storageDir = File(
                FileUtil.getExternalStorageDir(Environment.DIRECTORY_PICTURES, relativePath),
                displayName
            )
            // 创建实例
            return newInstance(
                writerClass,
                writerBundle,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values,
                storageDir.absolutePath
            )
        }

        /**
         * 为视频保存创建文件导出实例
         *
         * @param writerClass 文件写入操作者类
         * @param writerBundle 文件写入操作者参数
         * @param title 文件标题
         * @param description 文件描述
         * @param displayName 文件名
         * @param relativePath 文件保存相对父路径（通常用于标示不同的应用区分）
         */
        @Suppress("LongParameterList")
        fun <Writer : ContentValuesWriter> newInstanceForVideo(
            writerClass: Class<Writer>,
            writerBundle: Bundle?,
            title: String,
            description: String,
            displayName: String,
            relativePath: String
        ): FileExportFragment {
            val extName = FileUtil.getFileExtensionFromUrl(displayName)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extName)
            // 高版本的数据描述
            val values = ContentValues()
            values.put(MediaStore.Video.Media.TITLE, title)
            values.put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            values.put(MediaStore.Video.Media.DESCRIPTION, description)
            values.put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/$relativePath")
            // 低版本的保存路径
            val storageDir = File(
                FileUtil.getExternalStorageDir(Environment.DIRECTORY_MOVIES, relativePath),
                displayName
            )
            // 创建实例
            return newInstance(
                writerClass,
                writerBundle,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values,
                storageDir.absolutePath
            )
        }
    }
}
