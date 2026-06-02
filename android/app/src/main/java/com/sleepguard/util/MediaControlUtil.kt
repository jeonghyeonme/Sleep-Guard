package com.sleepguard.util

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent

class MediaControlUtil(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun pauseMedia() {
        if (audioManager.isMusicActive) {
            val eventTime = System.currentTimeMillis()
            
            // Send KEYCODE_MEDIA_PAUSE
            sendMediaButtonEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
            sendMediaButtonEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE)
            
            // Fallback for some apps: KEYCODE_MEDIA_PLAY_PAUSE
            sendMediaButtonEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            sendMediaButtonEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        }
    }

    private fun sendMediaButtonEvent(action: Int, keyCode: Int) {
        val event = KeyEvent(System.currentTimeMillis(), System.currentTimeMillis(), action, keyCode, 0)
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        intent.putExtra(Intent.EXTRA_KEY_EVENT, event)
        context.sendOrderedBroadcast(intent, null)
    }
}
