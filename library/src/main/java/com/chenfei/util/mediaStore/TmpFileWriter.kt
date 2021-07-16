package com.chenfei.util.mediaStore

import android.os.Bundle
import androidx.fragment.app.Fragment
import java.io.File
import java.io.OutputStream

/**
 * 临时文件导出保存逻辑
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-18 15:05
 */
class TmpFileWriter : ContentValuesWriter {
    private lateinit var tmpFile: File
    private var autoDeleteTmpFile: Boolean = false

    override fun parseArg(host: Fragment, arg: Bundle?): Boolean {
        super.parseArg(host, arg)
        arg ?: return false
        val tmpFilePath = arg.getString(TMP_FILE_PATH)
                ?: return false
        tmpFile = File(tmpFilePath)
        // 检查文件是否存在
        if (tmpFile.isDirectory || !tmpFile.exists()) {
            return false
        }
        autoDeleteTmpFile = arg.getBoolean(AUTO_DELETE_TMP_FILE)
        return true
    }

    override fun write(outputStream: OutputStream) {
        // 将临时文件写入目标
        tmpFile.inputStream()
                .buffered()
                .copyTo(outputStream)
        // 删除临时文件
        if (autoDeleteTmpFile) {
            tmpFile.delete()
        }
    }

    companion object {
        private const val TMP_FILE_PATH = "tmpFilePath"
        private const val AUTO_DELETE_TMP_FILE = "autoDeleteTmpFile"

        fun createBundle(tmpFile: File, autoDeleteTmpFile: Boolean = true) = Bundle().apply {
            putString(TMP_FILE_PATH, tmpFile.absolutePath)
            putBoolean(AUTO_DELETE_TMP_FILE, autoDeleteTmpFile)
        }
    }
}
