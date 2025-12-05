package io.github.chenfei0928.demo

import android.media.AudioManager
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat

/**
 * @author chenf()
 * @date 2024-12-26 11:23
 */
class AudioFocusService : LifecycleService() {
    private val am = getSystemService(AudioManager::class.java)

    override fun onCreate() {
        super.onCreate()
        Log.v(TAG, "onCreate: ")
        AudioManagerCompat.requestAudioFocus(am, audioFocusRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy: ")
        AudioManagerCompat.abandonAudioFocusRequest(am, audioFocusRequest)
    }

    private val audioFocusLis = AudioManager.OnAudioFocusChangeListener { focusChange ->
        Log.v(TAG, "onAudioFocusChange: $focusChange")
    }
    private val audioFocusRequest = AudioFocusRequestCompat.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setOnAudioFocusChangeListener(audioFocusLis)
        .setAudioAttributes(
            AudioAttributesCompat.Builder()
                .setLegacyStreamType(AudioManager.STREAM_VOICE_CALL)
                .build()
        )
        .build()

    companion object {
        private const val TAG = "AudioFocusService"
    }
}
