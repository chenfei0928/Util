package io.github.chenfei0928.demo

import android.media.AudioManager
import android.util.Log
import androidx.lifecycle.LifecycleService
import io.github.chenfei0928.media.AbstractAudioFocusRequester

/**
 * @author chenf()
 * @date 2024-12-26 11:23
 */
class AudioFocusService : LifecycleService() {
    override fun onCreate() {
        super.onCreate()
        Log.v(TAG, "onCreate: ")
        audioFocusRequest.requestAudioFocus(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy: ")
        audioFocusRequest.abandonAudioFocus(this)
    }

    private val audioFocusRequest = object : AbstractAudioFocusRequester(
        AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN
    ) {
        override fun onAudioFocusChange(focusChange: Int) {
            Log.v(TAG, "onAudioFocusChange: ${audioFocusToString(focusChange)}")
        }
    }

    companion object {
        private const val TAG = "AudioFocusService"
    }
}
