package com.chenfei.library.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.support.v4.util.ArrayMap
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * log日志统计保存
 *
 * @author MrFeng
 */
class FileLogUtil
private constructor(
        context: Context
) : Thread("FileLogUtil") {
    private val pid: String = android.os.Process.myPid().toString()
    private val logDirName: String = createLogDir(context)
    private val logFileName: String = getFileName(context)
    private val deviceInfo: String = getDevicesInfo(context)
    private val running = AtomicBoolean(true)

    /**
     * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
     * 显示当前mPID程序的 E和W等级的日志：logcat *:e *:w | grep "mPID"
     * 打印所有日志信息：logcat | grep "mPID"
     * 打印标签过滤信息：logcat -s way
     */
    private val cmd: String = "logcat | grep \"$pid\""
    val fullLogFileName: String = "$logDirName${File.separatorChar}$logFileName.log"

    fun stopLogs() {
        running.set(false)
    }

    override fun run() {
        try {
            // 执行logcat
            Runtime.getRuntime().exec(cmd).use { logcatProc ->
                File(fullLogFileName).bufferedWriter().use { bw ->
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
                            if (needWriteDate == null)
                                needWriteDate = needWriteData(it)
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
        private const val TAG = "FileLogUtil"
        @Volatile
        private var INSTANCE: FileLogUtil? = null
        private val dateEn = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        private val dataFormat: Map<String, SimpleDateFormat> by lazy {
            arrayOf("yyyy-MM-dd HH:mm:ss.SSS", "yy-MM-dd HH:mm:ss.SSS", "MM-dd HH:mm:ss.SSS",
                    "yyyy-MM-dd HH:mm:ss", "yy-MM-dd HH:mm:ss", "MM-dd HH:mm:ss")
                    .associateByTo(ArrayMap<String, SimpleDateFormat>(), { it },
                            { SimpleDateFormat(it, Locale.getDefault()) })
        }

        @JvmStatic
        fun getInstance(context: Context): FileLogUtil {
            if (INSTANCE == null) {
                INSTANCE = FileLogUtil(context)
            }
            return INSTANCE as FileLogUtil
        }

        private fun needWriteData(logLine: String): Boolean {
            return dataFormat
                    .filterKeys { logLine.length > it.length }
                    .map { it.value.parse(logLine.substring(0, it.key.length), ParsePosition(0)) }
                    .any { it != null }
        }

        @JvmStatic
        fun saveExceptionToLog(context: Context, throwable: Throwable): String {
            val file = File(createLogDir(context), getFileName(context) + ".log")
            try {
                PrintWriter(file, "utf-8").use { pw ->
                    pw.write(getDevicesInfo(context))
                    pw.write("\n")
                    pw.flush()
                    throwable.printStackTrace(pw)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "saveExceptionToLog: ", e)
            }
            return file.absolutePath
        }

        fun deleteAll(context: Context) {
            File(createLogDir(context)).deleteRecursively()
        }

        private fun createLogDir(context: Context): String {
            var logDir = context.getExternalFilesDir("log")
            if (logDir == null) {
                logDir = File(context.filesDir, "log")
            }
            if (!logDir.exists())
                logDir.mkdirs()
            return logDir.absolutePath
        }

        private fun getFileName(context: Context): String {
            val logTime = getDateEn().replace(' ', '_')

            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val processInfos = am.runningAppProcesses
            val myPid = android.os.Process.myPid()
            for (info in processInfos) {
                if (info.pid == myPid) {
                    return logTime + "_" + info.processName
                }
            }
            return logTime
        }

        // 2012-10-03 23:41:31.123
        fun getDateEn(): String {
            return dateEn.format(Date(System.currentTimeMillis()))
        }

        /**
         * 制造商/品牌/型号/修订版本/系统版本/编译时间/应用版本号
         * LGE/google/Nexus 5X/NPG47I/7.1.2/17-03-09_06:10/4.2.0
         */
        private fun getDevicesInfo(context: Context): String {
            return """
                |硬件制造商: ${Build.MANUFACTURER}
                |品牌：${Build.BRAND}
                |型号: ${Build.MODEL}
                |修订版本号: ${Build.ID}
                |OS 版本: ${Build.VERSION.RELEASE}
                |OS SDK 版本: ${Build.VERSION.SDK_INT}
                |OS 编译日期: ${Date(Build.TIME)}
                |OS 安全补丁级别: ${
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                Build.VERSION.SECURITY_PATCH
            else
                "-"
            }
                |应用版本号: ${context.packageManager.getPackageInfo(context.packageName, 0).versionCode}
                """.trimMargin()
        }
    }
}
