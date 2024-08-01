package io.github.chenfei0928.util

import android.content.Context
import android.os.Build
import androidx.collection.ArrayMap
import io.github.chenfei0928.app.RunningEnvironmentUtil
import io.github.chenfei0928.io.FileUtil
import io.github.chenfei0928.io.forEachLine
import java.io.File
import java.io.PrintWriter
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * log日志统计保存
 *
 * @author way
 */
class FileLogUtil
private constructor(
    context: Context
) : Thread("FileLogUtil") {
    private val deviceInfo: String by lazy { devicesInfoProvider(context) }
    private val running = AtomicBoolean(true)

    val fullLogFile: File = getLogFile(context)

    fun stopLogs() {
        running.set(false)
    }

    override fun run() {
        val pid: String = android.os.Process.myPid().toString()

        /**
         * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
         * 显示当前mPID程序的 E和W等级的日志：logcat *:e *:w | grep "mPID"
         * 打印所有日志信息：logcat | grep "mPID"
         * 打印标签过滤信息：logcat -s way
         */
        val cmd: String = "logcat | grep \"$pid\""
        Log.i(TAG, "FileLogUtil is running.")
        try {
            // 执行logcat
            Runtime.getRuntime().exec(cmd).use { logcatProc ->
                fullLogFile.bufferedWriter().use { bw ->
                    // 写入设备信息
                    bw.write(deviceInfo)
                    bw.write("\n")
                    bw.flush()
                    // 读取日志
                    var needWriteDate: Boolean? = null
                    logcatProc.inputStream.reader().forEachLine {
                        // 是否要继续运行
                        if (!running.get()) {
                            return@run
                        }
                        if (it.isNotEmpty()) {
                            if (needWriteDate == null) {
                                needWriteDate = needWriteData(it)
                            }
                            // 如果是当前进程的日志
                            if (it.contains(pid)) {
                                if (needWriteDate!!) {
                                    bw.write(getDateEn())
                                    bw.write(" ")
                                }
                                bw.write(it)
                                bw.write("\n")
                                bw.flush()
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "run: ", e)
        }
    }

    companion object {
        private const val TAG = "KW_FileLogUtil"

        @Volatile
        private var INSTANCE: FileLogUtil? = null
        var devicesInfoProvider: (context: Context) -> String = { "" }

        @JvmStatic
        fun getInstance(context: Context): FileLogUtil {
            if (INSTANCE == null) {
                INSTANCE = FileLogUtil(context)
            }
            return INSTANCE as FileLogUtil
        }

        //<editor-fold desc="判断日志中是否需要重写日期信息" defaultstatus="collapsed">
        private val dataFormat: Map<String, SimpleDateFormat> by lazy {
            arrayOf(
                "yyyy-MM-dd HH:mm:ss.SSS", "yy-MM-dd HH:mm:ss.SSS", "MM-dd HH:mm:ss.SSS",
                "yyyy-MM-dd HH:mm:ss", "yy-MM-dd HH:mm:ss", "MM-dd HH:mm:ss"
            )
                .associateByTo(ArrayMap(), { it },
                    { SimpleDateFormat(it, Locale.US) })
        }

        private fun needWriteData(logLine: String): Boolean {
            return dataFormat
                .filterKeys { logLine.length > it.length }
                .map { it.value.parse(logLine.substring(0, it.key.length), ParsePosition(0)) }
                .any { it != null }
        }
        //</editor-fold>

        /**
         * 写入异常信息到日志文件中并返回日志文件名
         */
        @JvmStatic
        fun saveExceptionToLog(context: Context, throwable: Throwable): String {
            val file = getLogFile(context)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PrintWriter(file, Charsets.UTF_8)
                } else {
                    PrintWriter(file, "utf-8")
                }.use { pw ->
                    pw.write(devicesInfoProvider(context))
                    pw.write("\n")
                    pw.flush()
                    throwable.printStackTrace(pw)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "saveExceptionToLog: ", e)
            }
            return file.absolutePath
        }

        /**
         * 删除所有日志文件
         */
        fun deleteAll(context: Context) {
            FileUtil.deleteFileOrDir(File(createLogDir(context)))
        }

        //<editor-fold desc="私有获取文件名工具方法" defaultstatus="collapsed">
        private fun createLogDir(context: Context): String {
            var logDir = context.getExternalFilesDir("log")
            if (logDir == null) {
                logDir = File(context.filesDir, "log")
            }
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            return logDir.absolutePath
        }

        private fun getLogFile(context: Context): File {
            val logDir = createLogDir(context)
            val logFileName = getFileName(context) + ".log"
            return File(logDir, logFileName)
        }

        private fun getFileName(context: Context): String {
            val logTime = getDateEn().replace(' ', '_')
                .replace(':', '-')

            val name = RunningEnvironmentUtil.getProcessName(context)
            if (name.isNotEmpty()) {
                return logTime + "_" + name
            }
            return logTime
        }

        private val dateEn: SimpleDateFormat by lazy {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        }

        // 2012-10-03 23:41:31.123
        private fun getDateEn(): String {
            return dateEn.format(Date(System.currentTimeMillis()))
        }
        //</editor-fold>
    }
}
