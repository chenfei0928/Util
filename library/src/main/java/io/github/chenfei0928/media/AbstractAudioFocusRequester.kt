package io.github.chenfei0928.media

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import android.util.Log
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi

/**
 * 音频焦点请求者，用于封装音频焦点切换操作
 *
 * @author MrFeng
 * @date 2018/1/31
 */
abstract class AbstractAudioFocusRequester
constructor(
    @param:StreamType
    private val streamType: Int,
    @param:FocusGain
    private val focusGain: Int,
) : OnAudioFocusChangeListener {
    @get:RequiresApi(api = Build.VERSION_CODES.O)
    private val audioFocusRequest: AudioFocusRequest by lazy {
        @SuppressLint("NewApi")
        AudioFocusRequest.Builder(focusGain)
            .setOnAudioFocusChangeListener(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setLegacyStreamType(streamType)
                    .build()
            )
            .build()
    }

    fun requestAudioFocus(context: Context) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return
        Log.d(TAG, "requestAudioFocus: ${audioFocusToString(focusGain)}")
        // 请求音频焦点
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            am.requestAudioFocus(audioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(this, streamType, focusGain)
        }
    }

    fun abandonAudioFocus(context: Context) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return
        Log.d(TAG, "abandonAudioFocus: ${audioFocusToString(focusGain)}")
        // 请求解除音频焦点，并且音频焦点已注册，解除音频焦点
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            am.abandonAudioFocusRequest(audioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(this)
        }
    }

    fun audioFocusToString(@FocusGain focus: Int): String {
        return when (focus) {
            AudioManager.AUDIOFOCUS_NONE -> "AUDIOFOCUS_NONE"
            AudioManager.AUDIOFOCUS_GAIN -> "AUDIOFOCUS_GAIN"
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> "AUDIOFOCUS_GAIN_TRANSIENT"
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK"
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE -> "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE"
            AudioManager.AUDIOFOCUS_LOSS -> "AUDIOFOCUS_LOSS"
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> "AUDIOFOCUS_LOSS_TRANSIENT"
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK"
            else -> "AUDIO_FOCUS_UNKNOWN($focus)"
        }
    }

    @IntDef(
        AudioManager.STREAM_VOICE_CALL,
        AudioManager.STREAM_SYSTEM,
        AudioManager.STREAM_RING,
        AudioManager.STREAM_MUSIC,
        AudioManager.STREAM_ALARM,
        AudioManager.STREAM_NOTIFICATION
    )
    annotation class StreamType

    @IntDef(
        AudioManager.AUDIOFOCUS_NONE,
        AudioManager.AUDIOFOCUS_GAIN,
        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE,
        AudioManager.AUDIOFOCUS_LOSS,
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
    )
    annotation class FocusGain

    companion object {
        private const val TAG = "KW_AbstractAudioFocusR"
    }
}
