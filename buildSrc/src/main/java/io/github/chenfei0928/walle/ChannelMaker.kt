package io.github.chenfei0928.walle

import com.google.common.io.Files
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 进行签名任务，参考自：
 * [com.meituan.android.walle.ChannelMaker.generateChannelApk]
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-12-10 15:10
 */
internal object ChannelMaker {

    /**
     * Apk file name format
     * [com.meituan.android.walle.Extension.apkFileNameFormat]
     */
    private var walleChannelApkFileNameFormat: String? =
        "app_\${channel}_\${buildType}_\${versionName}_\${buildTime}.apk"

    /**
     * 打入渠道的文件的输出文件夹名
     */
    internal const val CHANNELS_APK_OUTPUT_DIR_NAME = "channels"

    fun getChannelListFromFile(channelFile: File): List<String> =
        com.meituan.android.walle.ChannelMaker
            .getChannelListFromFile(channelFile) as List<String>

    //<editor-fold defaultstate="collapsed" desc="来自Walle的打入渠道包功能实现">
    private const val DOT_APK = ".apk"

    fun generateChannelApk(
        apkFile: File,
        channelOutputFolder: File,
        nameVariantMap: MutableMap<String, String?>,
        channel: String,
        extraInfo: Map<String, String>? = null,
        alias: String? = null
    ) {
        val buildTime = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val channelName = alias ?: channel

        val fileName = apkFile.name.let { fileName ->
            if (fileName.endsWith(DOT_APK)) {
                fileName.substring(0, fileName.lastIndexOf(DOT_APK))
            } else {
                fileName
            }
        }

        val apkFileName = "${fileName}-${channelName}$DOT_APK"

        val channelApkFile = File(channelOutputFolder, apkFileName)
        org.apache.commons.io.FileUtils.copyFile(apkFile, channelApkFile)
        com.meituan.android.walle.ChannelWriter.put(channelApkFile, channel, extraInfo)

        nameVariantMap["buildTime"] = buildTime
        nameVariantMap["channel"] = channelName
        nameVariantMap["fileSHA1"] = getFileHash(channelApkFile)
        val apkFileNameFormat = walleChannelApkFileNameFormat
        if (!apkFileNameFormat.isNullOrEmpty()) {
            val newApkFileName = groovy.text.SimpleTemplateEngine()
                .createTemplate(apkFileNameFormat)
                .make(nameVariantMap)
                .toString()
            if (!newApkFileName.contentEquals(apkFileName)) {
                channelApkFile.renameTo(File(channelOutputFolder, newApkFileName))
            }
        }
    }

    @Throws(java.io.IOException::class)
    private fun getFileHash(file: File): String {
        val hashFunction = com.google.common.hash.Hashing.sha1()
        val hashCode = if (file.isDirectory) {
            hashFunction.hashString(file.path, Charsets.UTF_16LE)
        } else {
            Files.asByteSource(file).hash(hashFunction)
        }
        return hashCode.toString()
    }
    //</editor-fold>
}
