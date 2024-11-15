package io.github.chenfei0928.media

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresPermission
import io.github.chenfei0928.util.Log
import java.io.IOException
import kotlin.math.log10

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-10-25 17:00
 */
class AudioRecorder
@RequiresPermission(Manifest.permission.RECORD_AUDIO)
constructor(
    context: Context,
    val fileName: String  // 录音生成的文件存储路径
) {

    // 录音类
    private var recorder: MediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        MediaRecorder()
    }

    fun startRecord(): Boolean {
        return try {
            recorder.setOnInfoListener { _, what, extra ->
                Log.i(TAG, "startRecord: info $what $extra")
            }
            recorder.setOnErrorListener { _, what, extra ->
                Log.i(TAG, "startRecord: error $what $extra")
            }
            // 音源
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            // 设置输出格式和编码格式
            recorder.setAudioSamplingRate(16000)
            recorder.setAudioEncodingBitRate(16)
            recorder.setAudioChannels(1)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            recorder.setOutputFile(fileName)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            // 准备并开始录音
            recorder.prepare()
            recorder.start()
            Log.i(TAG, "开始录音...")
            true
        } catch (e: IOException) {
            Log.e(TAG, "准备失败", e)
            false
        }
    }

    fun stopRecord() {
        recorder.stop()
        recorder.reset()
        recorder.release()
        Log.i(TAG, "停止录音")
    }

    /**
     * 获取声压（场的幅值）
     */
    private fun getMaxAmplitude() = try {
        recorder.maxAmplitude
    } catch (_: IllegalStateException) {
        0
    }

    // 初始值记录
    private var dbstart = 0f

    // 最新值
    private var dblast = dbstart

    fun getDb(): Float {
        val dbValue = 20 * log10(getMaxAmplitude().toFloat())
        // 最新值赋予以及保留两位小数
        dbstart = dblast + (dbValue - dblast) * 0.2f
        dblast = dbstart
        return dblast
    }

    companion object {
        private const val TAG = "KW_VoiceRecorder"
    }
}
