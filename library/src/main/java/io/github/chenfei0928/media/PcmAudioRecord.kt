package io.github.chenfei0928.media

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import io.github.chenfei0928.concurrent.coroutines.IoScope
import io.github.chenfei0928.util.Log
import kotlinx.coroutines.launch
import kotlin.math.log10

/**
 * 使用PCM直接进行录音，以实时获取音频原始数据
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-05 10:37
 */
class PcmAudioRecord
@RequiresPermission(Manifest.permission.RECORD_AUDIO)
constructor(
    sampleRateInHz: Int = 16_000,
    channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) {

    // 最小缓冲区大小（单位：字节）
    private val bufferSize: Int =
        AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
    private val audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat, bufferSize
    )
    val wavConverter: PcmToWavConverter by lazy(LazyThreadSafetyMode.NONE) {
        // 比特数
        val bitNum: Byte = when (audioFormat) {
            AudioFormat.ENCODING_PCM_8BIT -> 8
            AudioFormat.ENCODING_PCM_16BIT -> 16
            AudioFormat.ENCODING_PCM_FLOAT -> 32
            else -> throw IllegalArgumentException(
                "audioFormat $audioFormat must be ENCODING_PCM_8BIT or ENCODING_PCM_16BIT or ENCODING_PCM_FLOAT."
            )
        }
        // 声道数
        val channelCount = Integer
            .bitCount(channelConfig)
            .toByte()
        PcmToWavConverter(
            bufferSize,
            sampleRateInHz,
            channelCount,
            bitNum
        )
    }

    fun startRecordBy16Bit(@WorkerThread callback: (buffer: ShortArray, bufferReadSize: Int) -> Unit) {
        IoScope.launch {
            // 开始录音并创建缓冲区
            audioRecord.startRecording()
            val buffer = ShortArray(bufferSize)
            // 读取进入缓冲区
            while (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val bufferReadResult = audioRecord.read(buffer, 0, bufferSize)
                // 读取音频数据需要时间，在此期间其可能已被停止，不再需要回调，在此进行检查
                if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    callback(buffer, bufferReadResult)
                    calculateDb(buffer, bufferReadResult)
                }
            }
        }
    }

    fun startRecordBy8Bit(@WorkerThread callback: (buffer: ByteArray, bufferReadSize: Int) -> Unit) {
        IoScope.launch {
            // 开始录音并创建缓冲区
            audioRecord.startRecording()
            val buffer = ByteArray(bufferSize)
            // 读取进入缓冲区
            while (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val bufferReadResult = audioRecord.read(buffer, 0, bufferSize)
                // 读取音频数据需要时间，在此期间其可能已被停止，不再需要回调，在此进行检查
                if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    callback(buffer, bufferReadResult)
                    calculateDb(buffer, bufferReadResult)
                }
            }
        }
    }

    fun stopRecord() {
        audioRecord.stop()
        audioRecord.release()
    }

    private fun calculateDb(shorts: ShortArray, size: Int) {
        val volumeCallback = volumeCallback
            ?: return
        // 将 buffer 内容取出，进行平方和运算
        var sum = 0L
        for (i in 0 until size) {
            val sh = shorts[i]
            sum += sh * sh
        }
        // 平方和除以数据总长度，得到音量大小。
        val mean = sum / size.toDouble()
        val volume = 10 * log10(mean)
        Log.d(TAG, "分贝值:$volume")
        volumeCallback(volume)
    }

    private fun calculateDb(shorts: ByteArray, size: Int) {
        val volumeCallback = volumeCallback
            ?: return
        // 将 buffer 内容取出，进行平方和运算
        var sum = 0L
        for (i in 0 until size) {
            val sh = shorts[i]
            sum += sh * sh
        }
        // 平方和除以数据总长度，得到音量大小。
        val mean = sum / size.toDouble()
        val volume = 10 * log10(mean)
        Log.d(TAG, "分贝值:$volume")
        volumeCallback(volume)
    }

    /**
     * 音量回调，可能会是 [Double.POSITIVE_INFINITY] 或 [Double.NaN]
     */
    var volumeCallback: ((volume: Double) -> Unit)? = null

    companion object {
        private const val TAG = "KW_PcmAudioRecord"
    }
}
